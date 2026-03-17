package com.liv.domain;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.liv.api.dto.AuthLoginRequestDTO;
import com.liv.api.dto.AuthRegisterRequestDTO;
import com.liv.api.dto.AuthResponseDTO;
import com.liv.api.dto.NivelUsuarioDTO;
import com.liv.api.dto.UsuarioCreateRequestDTO;
import com.liv.api.dto.UsuarioResponseDTO;
import com.liv.api.dto.UsuarioUpdateRequestDTO;
import com.liv.api.security.AuthenticatedUser;
import com.liv.api.security.JwtService;
import com.liv.infra.repository.UsuarioRepository;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public UsuarioService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService
	) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponseDTO registrar(AuthRegisterRequestDTO request) {
		String email = normalizeEmail(request.email());

		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário cadastrado com este e-mail.");
		}

		Usuario usuario = new Usuario();
		usuario.setNome(normalizeName(request.nome()));
		usuario.setEmail(email);
		usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
		usuario.setNivel(usuarioRepository.count() == 0 ? NivelUsuario.ADMINISTRADOR : NivelUsuario.OPERADOR);
		usuario.setAtivo(true);
		usuario.setUltimoAcesso(new Date());

		usuarioRepository.save(usuario);

		return buildAuthResponse(usuario);
	}

	@Transactional
	public AuthResponseDTO autenticar(AuthLoginRequestDTO request) {
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas."));

		if (!usuario.isAtivo()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário inativo. Solicite liberação a um administrador.");
		}

		if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
		}

		usuario.setUltimoAcesso(new Date());
		usuarioRepository.save(usuario);

		return buildAuthResponse(usuario);
	}

	@Transactional(readOnly = true)
	public UsuarioResponseDTO buscarPerfil(Long id) {
		return UsuarioResponseDTO.fromEntity(getUsuarioAtivo(id));
	}

	@Transactional(readOnly = true)
	public List<UsuarioResponseDTO> listarUsuarios() {
		return usuarioRepository.findAllByOrderByNomeAsc()
				.stream()
				.map(UsuarioResponseDTO::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<NivelUsuarioDTO> listarNiveis() {
		return Arrays.stream(NivelUsuario.values())
				.map(nivel -> new NivelUsuarioDTO(nivel.name(), nivel.getLabel()))
				.toList();
	}

	@Transactional
	public UsuarioResponseDTO criarUsuario(UsuarioCreateRequestDTO request) {
		String email = normalizeEmail(request.email());

		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário cadastrado com este e-mail.");
		}

		Usuario usuario = new Usuario();
		usuario.setNome(normalizeName(request.nome()));
		usuario.setEmail(email);
		usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
		usuario.setNivel(request.nivel());
		usuario.setAtivo(true);

		return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
	}

	@Transactional
	public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioUpdateRequestDTO request, AuthenticatedUser authenticatedUser) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

		String nome = request.nome() == null || request.nome().isBlank() ? usuario.getNome() : normalizeName(request.nome());
		String email = request.email() == null || request.email().isBlank() ? usuario.getEmail() : normalizeEmail(request.email());
		NivelUsuario nivel = request.nivel() == null ? usuario.getNivel() : request.nivel();
		boolean ativo = request.ativo() == null ? usuario.isAtivo() : request.ativo();

		if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um usuário cadastrado com este e-mail.");
		}

		if (authenticatedUser.id().equals(id) && !ativo) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode desativar a sua própria conta.");
		}

		validateLastAdmin(usuario, nivel, ativo);

		usuario.setNome(nome);
		usuario.setEmail(email);
		usuario.setNivel(nivel);
		usuario.setAtivo(ativo);

		if (request.senha() != null && !request.senha().isBlank()) {
			usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
		}

		return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
	}

	@Transactional(readOnly = true)
	public Usuario getUsuarioAtivo(Long id) {
		return usuarioRepository.findByIdAndAtivoTrue(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado ou inativo."));
	}

	private AuthResponseDTO buildAuthResponse(Usuario usuario) {
		return new AuthResponseDTO(jwtService.generateToken(usuario), UsuarioResponseDTO.fromEntity(usuario));
	}

	private void validateLastAdmin(Usuario usuario, NivelUsuario nextLevel, boolean nextActive) {
		if (usuario.getNivel() != NivelUsuario.ADMINISTRADOR) {
			return;
		}

		boolean losingAdminPrivileges = !nextActive || nextLevel != NivelUsuario.ADMINISTRADOR;

		if (!losingAdminPrivileges) {
			return;
		}

		long activeAdmins = usuarioRepository.countByNivelAndAtivoTrue(NivelUsuario.ADMINISTRADOR);

		if (usuario.isAtivo() && activeAdmins <= 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "É necessário manter pelo menos um administrador ativo.");
		}
	}

	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	private String normalizeName(String nome) {
		return nome == null ? null : nome.trim();
	}
}
