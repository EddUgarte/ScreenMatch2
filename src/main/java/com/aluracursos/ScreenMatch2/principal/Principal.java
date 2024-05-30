package com.aluracursos.ScreenMatch2.principal;

import com.aluracursos.ScreenMatch2.model.*;
import com.aluracursos.ScreenMatch2.repository.SerieRepository;
import com.aluracursos.ScreenMatch2.service.ConsumoAPI;
import com.aluracursos.ScreenMatch2.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=909c4a25";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series;
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repositorio = repository;
    }

    public Principal() {
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar una serie
                    2 - Buscar las temporadas y episodios de una serie de la BD
                    3 - Mostrar historial de series buscadas
                    4 - Buscar series por titulo
                    5 - Top 5 mejores series
                    6 - Buscar Series por categoría
                    7 - Filtrar series por temporadas y evaluación
                    8 - Buscar episodios por titulo
                    9 - Top 5 episodios de una Serie
                                  
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriesPorCategoria();
                    break;
                case 7:
                    filtrarSeriesPorTemporadaYEvaluacion();
                    break;
                case 8:
                    buscarEpisodiosPorTitulo();
                    break;
                case 9:
                    buscarTop5Episodios();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }

    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json); //Imprime el json de la solicitud a la API
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        /*
        //Bloque de codigo para buscar episodios directo en la API
        DatosSerie datosSerie = getDatosSerie();
        List<DatosTemporadas> temporadas = new ArrayList<>();

        for (int i = 1; i <= datosSerie.totalTemporadas(); i++) {
            var json = consumoApi.obtenerDatos(URL_BASE + datosSerie.titulo().replace(" ", "+") + "&season=" + i + API_KEY);
            DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
            temporadas.add(datosTemporada);
        }
        temporadas.forEach(System.out::println);
        */

        //Bloque de codigo para buscar episodios en mi Base de Datos
        mostrarSeriesBuscadas(); //Muestra las listas actuales de mi BD
        System.out.println("Escribe el nombre de la seria de la cual quieres ver los episodios");
        var nombreSerie = teclado.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if(serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }

    }

    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        //datosSeries.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {
        series = repositorio.findAll();
        /*
        List<Serie> series = new ArrayList<>();
        series = datosSeries.stream()
                .map(d -> new Serie(d))
                .collect(Collectors.toList());
        */
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriesPorTitulo(){
        System.out.println("Escribe el nombre de la serie que deseas buscar: ");
        var nombreSerie = teclado.nextLine();
        serieBuscada = repositorio.findByTituloContainsIgnoreCase(nombreSerie);

        if(serieBuscada.isPresent()){
            System.out.println("La serie buscada es: " + serieBuscada.get());
        } else {
            System.out.println("Serie no encontrada");
        }

    }

    private void buscarTop5Series(){
        List<Serie> topSeries = repositorio.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s ->
                System.out.println("Serie: " + s.getTitulo() + " Evaluacion: " + s.getEvaluacion()) );
    }

    private void buscarSeriesPorCategoria(){
        System.out.println("Escriba el genero/categoría de la serie que desea buscar: ");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspanol(genero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Series de la categoría " + genero);
        seriesPorCategoria.forEach(System.out::println);
    }

    public void filtrarSeriesPorTemporadaYEvaluacion(){
        System.out.println("¿Filtrar series con cuántas temporadas como máximo? ");
        var totalTemporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("¿Con evaluación a partir de qué valor? ");
        var evaluacion = teclado.nextDouble();
        teclado.nextLine();
        List<Serie> filtroSeries = repositorio.findByTotalTemporadasLessThanEqualAndEvaluacionGreaterThanEqual(totalTemporadas,evaluacion);
        System.out.println("*** Series filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - evaluacion: " + s.getEvaluacion()));
    }

    private void  buscarEpisodiosPorTitulo(){
        System.out.println("Escribe el nombre del episodio que deseas buscar:");
        var nombreEpisodio = teclado.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Serie: %s Temporada %s Episodio %s Evaluación %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(), e.getNumeroEpisodio(), e.getEvaluacion()));

    }

    private void buscarTop5Episodios(){
        buscarSeriesPorTitulo();
        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = repositorio.top5Episodios(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Serie: %s - Temporada %s - Episodio %s - Evaluación %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(), e.getTitulo(), e.getEvaluacion()));

        }
    }


}
