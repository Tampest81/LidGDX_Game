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

	// Variáveis das texturas
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Texture logo;

	private Texture coinAtual;
	private Texture coin;

	// Variáveis para os colisores
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	private Rectangle coinCollider;

	// Variáveis para determinar a dimensão do dispositivo
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 2;

	private float coinPosicaoHorizontal;
	private float coinPosicaoVertical;

	private Texture coinSilver;

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

	// Variáveis para a fonte dos textos
	BitmapFont textoPontuacao;
	BitmapFont textoReinciar;
	BitmapFont textoMelhorPontuacao;

	// Variáveis para os sons
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	Preferences preferencias;

	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1200;

	double randomValue;

	// Semelhante ao Start da unity, será chamado uma vez no começo da aplicação
	@Override
	public void create ()
	{
		inicializarTexturas();
		inicializaObjetos();
	}
	// Semelhante ao Update da unity, será chamado várias vezes por segundo
	@Override
	public void render ()
	{
		// Limpeza de resíduos.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// Métodos para o jogo poder funcionar
		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}
	// Pega as texturas dentro da pasta Assets e atribui para dentro da varíavel
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

		logo = new Texture("logo.png");

		coin = new Texture("coin.png");
		coinSilver = new Texture("coin_silver.png");

	}
	// Define os objetos inicializados
	private void inicializaObjetos()
	{
		coinAtual = coinSilver;
		batch = new SpriteBatch();
		random = new Random();
		// Define algumas variáveis
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVeticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;

		coinPosicaoHorizontal = posicaoCanoHorizontal + larguraDispositivo / 2;

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

		coinCollider = new Rectangle();

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

	// Verifica o estado do jogo, sendo :
	// 0 -> Aguardando o input do jogador
	// 1 -> In game
	// 2 -> Game Over
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
			// Toca um som toda vez que ele pula
			if (toqueTela)
			{
				gravidade = -15;
				somVoando.play();
			}

			// Gravidade horizontal aplicada na moeda e no cano
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			coinPosicaoHorizontal -= Gdx.graphics.getDeltaTime() * 200;

			// Se o cano chegar no limite esquerdo da tela (logo atrás do jogador)
			// Faz o objeto aparecer na direito com uma altura aleatória
			// Desativa a verificação passouCano, que é utilizado para a adição da pontuação ao passar o cano
			if (posicaoCanoHorizontal < -canoTopo.getWidth())
			{
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400)-200;

				passouCano = false;
			}
			if (coinPosicaoHorizontal < -coin.getWidth())
			{
				// Randomiza um valor e retorna ele (entre 0-1)
				randomValue = Math.floor(Math.random()*2);
				if (randomValue == 0)
				{
					coinAtual = coinSilver;
				}
				else if (randomValue == 1)
				{
					coinAtual = coin;
				}

				coinPosicaoHorizontal = larguraDispositivo;
				coinPosicaoVertical = random.nextInt(400)-200;
			}


			// Caso o personagem tenha uma posição Y > 0 e o jogador dê um input, ele irá realizar um "pulo"
			if (posicaoInicialVeticalPassaro > 0 || toqueTela)
			{
				posicaoInicialVeticalPassaro = posicaoInicialVeticalPassaro - gravidade;
			}
			// Gravidade vertical para o player
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

	// Metodo que detecta e gera as colisões e colisores
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
						posicaoCanoHorizontal,
						alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
						canoTopo.getWidth(), canoTopo.getHeight()
				);
		// Colisor da moeda
		coinCollider.set
				(
						coinPosicaoHorizontal + coinAtual.getWidth() / 2,
						coinPosicaoVertical + coinAtual.getHeight() / 2,
						coinAtual.getWidth()/2,coinAtual.getHeight()/2
				);

		// Flags que definem se ocorreu uma sobreposição dos colisores
		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);

		boolean colidiuMoeda = Intersector.overlaps(circuloPassaro,coinCollider);

		// Se colidir com a moeda...
		if(colidiuMoeda)
		{
			// Gambiarra para retirar a moeda da tela
			coinPosicaoHorizontal -= Gdx.graphics.getDeltaTime() * 90000;

			// Verifica se é moeda de prata ou de ouro
			if(randomValue == 1) {
				pontos += 10;
			}
			else if (randomValue == 0){
				pontos += 5;
			}
		}
		// Caso ocorra alguma sobreposição do player com um cano, irá mudar o estado do jogo para 2 (2 = Tela de Game Over)
		if (colidiuCanoCima || colidiuCanoBaixo)
		{
			if (estadoJogo == 1)
			{
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}
	// Método que desenha os objetos na tela
	private void desenharTexturas()
	{

		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		// Desenha o fundo
		batch.draw(fundo,0,0,larguraDispositivo,alturaDispositivo);

		// Desenha a moeda
		batch.draw
				(
						coinAtual,
						coinPosicaoHorizontal,
						coinPosicaoVertical
				);


		// Desenha o passaro
		batch.draw
				(
				passaros[(int) variacao],
				50 + posicaoHorizontalPassaro,
				posicaoInicialVeticalPassaro
				);
		// Desenha o cano de baixo
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
		// Desenha o cano de cima
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
		// Desenha o texto da pontuação
		textoPontuacao.draw
				(
				batch,
				String.valueOf(pontos),
				larguraDispositivo/2,
				alturaDispositivo - 110
				);


		// Se o estado do jogo for 0, irá desenhar a Logo
		if (estadoJogo == 0)
		{
			batch.draw(logo, larguraDispositivo / 2 - logo.getWidth() / 2,
					alturaDispositivo /1.5f);
		}
		// Se o estado do jogo for 2 (2 = GameOver), irá desenhar os textos de gameover na tela
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
		// Caso o jogador passe o cano, ele irá ganhar pontos
		if(posicaoCanoHorizontal < 50-passaros[0].getWidth())
		{
			if (!passouCano)
			{
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		// Faz a animação do jogador e o seu tempo entre as animações
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
