package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private  List<DadosSerie> dadosSeries = new ArrayList<>();
    private Optional<Serie> serieBuscada;

    //responsavel por conectar no banco
    private SerieRepository repository;

    List<Serie> series = new ArrayList<>();
    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0){
        String menu = """
                1 - Buscar séries
                2 - Buscar episódios
                3 - Listar series buscadas
                4 - Buscar serie por titulo
                5 - Buscar series por ator
                6 - Buscar top 5 series
                7 - Buscar series por categoria
                8 - Buscar serie por quantidade de temporadas e avaliação
                9 - Buscar episodio por trecho de nome
                10 - Top 5 episodios
                11 - Buscar episódios a partir de uma data
                
                0 - Sair                                 
                """;

        System.out.println(menu);
        opcao = leitura.nextInt();
        leitura.nextLine();

        switch (opcao) {
            case 1:
                buscarSerieWeb();
                break;
            case 2:
                buscarEpisodioPorSerie();
                break;
            case 3:
                listarSeriesBuscadas();
                break;
            case 4:
                buscarSeriePorTitulo();
                break;
            case 5:
                buscarSeriePorAtor();
                break;
            case 6:
                buscarTop5Series();
                break;
            case 7:
                buscarSeriesPorCategoria();
                break;
            case 8:
                buscarSeriesPorTemporadaAndAvaliacao();
                break;
            case 9:
                buscarEpisodioPorTrecho();
                break;
            case 10:
                top5EpisodiosPorSerie();
                break;
            case 11:
                buscarEpisodiosDepoisDeUmaData();
                break;
            case 0:
                System.out.println("Saindo...");
                break;
            default:
                System.out.println("Opção inválida");
        }
        }
    }



    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        //colocando serie no banco
        Serie serie = new Serie(dados);
        repository.save(serie);

//        dadosSeries.add(dados);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma serie pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> first = repository.findByTituloContainingIgnoreCase(nomeSerie);
//        Optional<Serie> first = series.stream()
//                .filter(serie -> serie.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
//                .findFirst();

        if (first.isPresent()){
            var serieEncontrada = first.get();
            List<DadosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(dadosTemporada -> dadosTemporada.episodios().stream()
                            .map(dadosEpisodio -> new Episodio(dadosTemporada.numero(), dadosEpisodio)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repository.save(serieEncontrada);
        } else {
            System.out.println("Serie não encontrada!");
        }


    }
    private void listarSeriesBuscadas(){
        //pegando Serie do banco
        series = repository.findAll();
//        series = dadosSeries.stream()
//                .filter(dadosSerie -> dadosSerie.titulo() != null)
//                .map(dadosSerie -> new Serie(dadosSerie))
//                .collect(Collectors.toList());

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }
    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma serie pelo nome: ");
        var nome = leitura.nextLine();

        serieBuscada = repository.findByTituloContainingIgnoreCase(nome);
        if (serieBuscada.isPresent()){
            System.out.println("Dados da serie: " + serieBuscada.get());
        } else {
            System.out.println("Serie não encontrada!");
        }

    }
    private void buscarSeriePorAtor() {
        System.out.println("Busque series pelo nome do ator: ");
        var nomeAtor = leitura.nextLine();

        List<Serie> seriesBuscada = repository.findByAtoresContainingIgnoreCase(nomeAtor);

        if (!seriesBuscada.isEmpty()) {
            System.out.println("Serie que " + nomeAtor + " trabalhou: ");
            seriesBuscada.stream()
                    .forEach(serie -> System.out.println(serie.getTitulo() + " Avaliação: " +serie.getAvaliacao()));
        } else {
            System.out.println("Serie não encontrada!");
        }

    }
    private void buscarTop5Series() {
        List<Serie> seriesTopCinco = repository.findTop5ByOrderByAvaliacaoDesc();
        System.out.println("Top 5 series: ");
        seriesTopCinco.stream()
                .forEach(serie -> System.out.println(serie.getTitulo() + " Avaliação: " +serie.getAvaliacao()));
    }
    private void buscarSeriesPorCategoria() {
        System.out.println("Digite uma categoria para ser buscada: ");
        var categoriaBuscada = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(categoriaBuscada);
        List<Serie> seriesPorCategoria = repository.findSerieByGenero(categoria);

        seriesPorCategoria.forEach(System.out::println);
    }
    private void buscarSeriesPorTemporadaAndAvaliacao() {
        System.out.println("Digite o numero de temporadas: ");
        Integer numeroTemporada = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Digite o numero de avaliação: ");
        Double avaliacao = leitura.nextDouble();

//        List<Serie> seriePorTemporadaAvaliacao = repository.findSerieByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(numeroTemporada, avaliacao);
        List<Serie> seriePorTemporadaAvaliacao = repository.seriesPorTemporadasEAvaliacao(numeroTemporada, avaliacao);
        seriePorTemporadaAvaliacao.forEach(System.out::println);

    }
    private void buscarEpisodioPorTrecho() {
        System.out.println("Digite um trecho para busca do episodio");
        String trecho = leitura.nextLine();
        List<Episodio> episodiosPorTrecho = repository.episodioPorTrecho(trecho);
        episodiosPorTrecho.forEach(System.out::println);

    }
    private void top5EpisodiosPorSerie() {
        System.out.println("Escolha uma serie pelo nome: ");
        String nomeSerie = leitura.nextLine();

        List<Episodio> episodios;
        Optional<Serie> serieBuscada = repository.findByTituloContainingIgnoreCase(nomeSerie);
        if (serieBuscada.isPresent()){
            episodios = repository.top5Episodios(serieBuscada.get());
            episodios.forEach(System.out::println);
        } else {
            System.out.println("Serie não encontrada!");
        }

    }
    private void buscarEpisodiosDepoisDeUmaData(){
        buscarSeriePorTitulo();
        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            System.out.println("Digite o ano limite de lançamento");
            var anoLancamento = leitura.nextInt();
            leitura.nextLine();

            List<Episodio> episodiosAno = repository.episodiosPorSerieEAno(serie, anoLancamento);
            episodiosAno.forEach(System.out::println);
        }
    }


}
