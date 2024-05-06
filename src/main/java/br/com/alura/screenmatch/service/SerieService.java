package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SerieService {
    @Autowired
    private SerieRepository repository;

    public List<SerieDTO> ObterTodasAsSeries(){
        return converteDados(repository.findAll());
    }

    public List<SerieDTO> ObterTop5Series() {
        return converteDados(repository.findTop5ByOrderByAvaliacaoDesc());
    }

    private List<SerieDTO> converteDados(List<Serie> series){
        return series.stream()
                .map(serie -> new SerieDTO(serie.getId(), serie.getTitulo(), serie.getTotalTemporadas(),
                        serie.getAvaliacao(), serie.getGenero(), serie.getAtores(), serie.getPoster(), serie.getSinopse()))
                .collect(Collectors.toList());

    }

    public List<SerieDTO> ObterLancamentos() {
        return converteDados(repository.lancamentosMaisRecentes());
    }

    public SerieDTO ObterPorId(Long id) {
        Optional<Serie> serie = repository.findById(id);
        if (serie.isPresent()){
            Serie s = serie.get();

            return new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(),
                    s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse());
        }
        return null;
    }

    public List<EpisodioDTO> ObterTemporadas(Long id) {
        Optional<Serie> serie = repository.findById(id);
        if (serie.isPresent()){
            Serie s = serie.get();
            return s.getEpisodios().stream()
                    .map(e -> new EpisodioDTO(e.getTemporada(), e.getNumeroEpisodio(), e.getTitulo()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<EpisodioDTO> ObterEpisodiosPorTemporada(Long id, Long temporada) {
        return repository.obterEpisodiosPorTemporada(id, temporada).stream()
                .map(episodio -> new EpisodioDTO(episodio.getTemporada(), episodio.getNumeroEpisodio(), episodio.getTitulo()))
                .collect(Collectors.toList());
    }

    public List<SerieDTO> ObterSeriesPorCategoria(String categoria) {
        Categoria cat = Categoria.fromPortugues(categoria);
        return converteDados(repository.findByGenero(cat));
    }

    public List<EpisodioDTO> ObterTop5Episodios(Long id) {
        Optional<Serie> serie = repository.findById(id);
        if (serie.isPresent()){
            Serie s = serie.get();
            return repository.top5Episodios(s).stream()
                    .map(episodio -> new EpisodioDTO(episodio.getTemporada(), episodio.getNumeroEpisodio(), episodio.getTitulo()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}
