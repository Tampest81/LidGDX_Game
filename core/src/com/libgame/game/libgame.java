package com.libgame.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class libgame extends ApplicationAdapter {

	// Atribuição das variáveis
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 2;
	private float posicaoInicialVeticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;

	BitmapFont textoPontuacao;
	BitmapFont textoReinciar;
	BitmapFont textoMelhorPontuacao;

	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	Preferences preferencias;

	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1200;

	// Start
	@Override
	public void create ()
	{
		inicializarTexturas();
		inicializaObjetos();
	}
	// Update
	@Override
	public void render ()
	{
		// Limpeza de resíduos.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}
	// Instancia as sprites e tambem adiciona as sprites p/ a animação
	private void inicializarTexturas()
	{
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");

	}
	// Define os objetos inicializados
	private void inicializaObjetos()
	{
		batch = new SpriteBatch();
		random = new Random();
		// Define algumas variáveis
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVeticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;
		// Define os textos, instancia eles, define uma cor e o tamanho
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReinciar = new BitmapFont();
		textoReinciar.setColor(Color.GREEN);
		textoReinciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);
		// Define hitboxes
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		// Define os audios
		somVoando = Gdx.audio.newSound( Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound( Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound( Gdx.files.internal("som_pontos.wav"));

		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH,VIRTUAL_HEIGHT,camera);
	}

	private void verificarEstadoJogo()
	{
		// Verifica o input do usuário (Touch)
		boolean toqueTela = Gdx.input.justTouched();
		// Caso o estado do jogo seja 0 e receba um input.... (0 = Aguardando o usuário receber o input)
		if(estadoJogo == 0)
		{
			// Caso receba o input, faz o personagem pular uma vez e muda de estado.
			if(toqueTela)
			{
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		}
		// Caso o estado do jogo seja 1 e receba um input.... (1 = Quando está em partida e recebe o input)
		else if (estadoJogo == 1)
		{
			// Caso receba o input, faz o personagem pular uma vez
			if (toqueTela)
			{
				gravidade = -15;
				somVoando.play();
			}
			// Instancia os canos e a sua posição
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;

			if (posicaoCanoHorizontal < -canoTopo.getWidth())
			{
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400)-200;
				passouCano = false;
			}
			// Caso o personagem tenha uma posição Y > 0 e o jogador dê um input, ele irá realizar um "pulo"
			if (posicaoInicialVeticalPassaro > 0 || toqueTela)
			{
				posicaoInicialVeticalPassaro = posicaoInicialVeticalPassaro - gravidade;
			}
			// Gravidade
			gravidade++;
		}
		// Caso o estado do jogo seja 2.... (2 = Tela de GameOver)
		else if(estadoJogo == 2)
		{
			// Caso a pontuação atual seja maior q a potuação máxima atingida, a pontuação máxima será alterada para a atual
			if(pontos > pontuacaoMaxima)
			{
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima",pontuacaoMaxima);
				preferencias.flush();
			}
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;

			// Caso o usuário dê o input, reinicia o jogo, redefine tudo para o padrão
			if (toqueTela)
			{
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVeticalPassaro = alturaDispositivo /2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}
	}

	// Metodo que detecta Colisoes
	private void detectarColisoes()
	{
		// Colisor do passaro
		circuloPassaro.set
				(
						50 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2,
						posicaoInicialVeticalPassaro + passaros[0].getHeight() / 2,
						passaros[0].getWidth() / 2
				);
		// Colisor do cano de baixo
		retanguloCanoBaixo.set
				(
						posicaoCanoHorizontal,
						alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
						canoBaixo.getWidth(), canoBaixo.getHeight()
				);
		// Colisor do cano de cima
		retanguloCanoCima.set
				(
						posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
						canoTopo.getWidth(), canoTopo.getHeight()
				);

		// Flags que definem se ocorreu uma sobreposição dos colisores
		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);

		// Caso ocorra alguma sobreposição, irá mudar o estado do jogo para 2 (2 = Tela de Game Over)
		if (colidiuCanoCima || colidiuCanoBaixo)
		{
			if (estadoJogo == 1)
			{
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}

	private void desenharTexturas()
	{
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(fundo,0,0,larguraDispositivo,alturaDispositivo);
		// Desenha o passaro
		batch.draw
				(
				passaros[(int) variacao],
				50 + posicaoHorizontalPassaro,
				posicaoInicialVeticalPassaro
				);
		// Desenha o cano
		batch.draw
				(
				canoBaixo,
				posicaoCanoHorizontal,
				alturaDispositivo / 2
						-
						canoBaixo.getHeight()
						-
						espacoEntreCanos/2
						+
						posicaoCanoVertical
				);

		batch.draw
				(
				canoTopo,
				posicaoCanoHorizontal,
				alturaDispositivo / 2
						+
						espacoEntreCanos / 2
						+
						posicaoCanoVertical
				);

		textoPontuacao.draw
				(
				batch,
				String.valueOf(pontos),
				larguraDispositivo/2,
				alturaDispositivo - 110
				);


		if (estadoJogo == 2)
		{
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2,
					alturaDispositivo / 2);
			textoReinciar.draw(batch,
					"Toque para reiniciar!", larguraDispositivo/ 2 - 140,
					alturaDispositivo/2 - gameOver.getHeight()/ 2);
			textoMelhorPontuacao.draw(batch,
					"Seu record é: " + pontuacaoMaxima + " pontos",
					larguraDispositivo/2 -140, alturaDispositivo/2 - gameOver.getHeight());
		}
		batch.end();
	}

	public void validarPontos()
	{
		if(posicaoCanoHorizontal < 50-passaros[0].getWidth())
		{
			if (!passouCano)
			{
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;

		if (variacao > 3)
		{
			variacao = 0;
		}
	}

	@Override
	public void resize(int width,int height)
	{
		viewport.update(width,height);
	}

	@Override
	public void dispose()
	{

	}
}
