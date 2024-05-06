package br.com.alura.screenmatch.repository;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

//responsavel por conectar no banco
public interface SerieRepository extends JpaRepository<Serie, Long> {
    Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);
    List<Serie> findByAtoresContainingIgnoreCase(String nomeAtor);
    List<Serie> findTop5ByOrderByAvaliacaoDesc();
    List<Serie> findSerieByGenero(Categoria categoria);

    List<Serie> findByGenero(Categoria categoria);

    List<Serie> findSerieByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(Integer temporadas, Double avaliacao);

    @Query("SELECT s FROM Serie s WHERE s.totalTemporadas <= :temporadas AND s.avaliacao >= :avaliacao")
    List<Serie> seriesPorTemporadasEAvaliacao(Integer temporadas, Double avaliacao);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:trecho%")
    List<Episodio> episodioPorTrecho(String trecho);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie ORDER BY e.avaliacao DESC LIMIT 5")
    List<Episodio> top5Episodios(Serie serie);

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie AND YEAR(e.dataLancamento) >= :anoLancamento")
    List<Episodio> episodiosPorSerieEAno(Serie serie, int anoLancamento);

    @Query("SELECT s FROM Serie s " +
            "JOIN s.episodios e " +
            "GROUP BY s " +
            "ORDER BY MAX(e.dataLancamento) DESC LIMIT 5")
    List<Serie> lancamentosMaisRecentes();

    @Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s.id = :id AND e.temporada = :temporada")
    List<Episodio> obterEpisodiosPorTemporada(Long id, Long temporada);


}
