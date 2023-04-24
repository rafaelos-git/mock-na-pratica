package valueprojects.mock_na_pratica;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.value.jogo.builder.CriadorDeJogo;
import dominio.Jogo;
import dominio.Participante;
import dominio.Resultado;
import dominio.Sms;
import infra.JogoDao;
import infra.SmsDaoFalso;
import infra.VencedorDaoFalso;
import service.EnviaSms;
import service.FinalizaJogo;
import service.Juiz;

import static org.mockito.Mockito.*;

public class FinalizaJogoTest {

	@Test
	public void deveFinalizarJogosDaSemanaAnterior() {

		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Jogo jogo1 = new CriadorDeJogo().para("Ca�a moedas")
				.naData(antiga).constroi();
		Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras")
				.naData(antiga).constroi();

		List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);

		JogoDao daoFalso = mock(JogoDao.class);

		when(daoFalso.emAndamento()).thenReturn(jogosAnteriores);

		FinalizaJogo finalizador = new FinalizaJogo(daoFalso);
		finalizador.finaliza();

		assertTrue(jogo1.isFinalizado());
		assertTrue(jogo2.isFinalizado());
		assertEquals(2, finalizador.getTotalFinalizados());
	}

	@Test
	public void deveVerificarSeMetodoAtualizaFoiInvocado() {

		Calendar antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		Jogo jogo1 = new CriadorDeJogo().para("Cata moedas").naData(antiga).constroi();
		Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras").naData(antiga).constroi();

		List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);

		JogoDao daoFalso = mock(JogoDao.class);

		when(daoFalso.emAndamento()).thenReturn(jogosAnteriores);

		FinalizaJogo finalizador = new FinalizaJogo(daoFalso);
		finalizador.finaliza();

		verify(daoFalso, times(1)).atualiza(jogo1);
	}

	@Test
	public void deveEnviarSmsAoVencedorApósFinalizarESalvarJogoDaSemanaAnteriorESalvarVencedor() {

		Calendar antiga = Calendar.getInstance();
		antiga.set(2022, 3, 28);

		// Cria um Jogo
		Jogo jogo = new CriadorDeJogo().para("Squid Game").naData(antiga).constroi();

		// Estabele 3 Resultados, cada um com um Participante diferente
		jogo.anota(new Resultado(new Participante("Steve Jobs"), 2000));
		jogo.anota(new Resultado(new Participante("Steve Wozniak"), 3000));
		jogo.anota(new Resultado(new Participante("Seong Gi-hun"), 456000));

		// Mocka um DAO falso do Jogo
		JogoDao jogoDaoFalso = mock(JogoDao.class);

		// Finaliza o jogo da semana anterior
		FinalizaJogo finalizador = new FinalizaJogo(jogoDaoFalso);
		finalizador.finaliza();

		// Salva o jogo finalizado no DAO falso e verifica se o método salva foi chamado
		jogoDaoFalso.salva(jogo);
		verify(jogoDaoFalso, times(1)).salva(jogo);

		// Instancia um Juiz e avalia o jogo
		Juiz juiz = new Juiz();
		juiz.avalia(jogo);

		// Pega o vencedor após a avaliação feita pelo juiz
		Participante vencedor = juiz.getTresMaiores().get(0).getParticipante();

		// Mock um DAO falso para salvar o vencedor
		VencedorDaoFalso vencedorDaoFalso = mock(VencedorDaoFalso.class);

		// Salva o vencedor no DAO falso e verifica se o método salvar foi chamado
		vencedorDaoFalso.salva(vencedor);
		verify(vencedorDaoFalso, times(1)).salva(vencedor);

		// Mocka o serviço EnviaSms e verifica se o método enviarSmsParaVencedor foi chamado e, assim,
		// o SMS foi enviado ao vencedor
		EnviaSms enviaSms = mock(EnviaSms.class);

		Sms smsVencedor = new Sms();
		enviaSms.enviarSmsParaVencedor(smsVencedor, vencedor);
		verify(enviaSms, times(1)).enviarSmsParaVencedor(smsVencedor, vencedor);
	}
}
