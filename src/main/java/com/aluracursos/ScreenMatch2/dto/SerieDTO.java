package com.aluracursos.ScreenMatch2.dto;

import com.aluracursos.ScreenMatch2.model.Categoria;


public record SerieDTO (
         Long id,
         String titulo,
         Integer totalTemporadas,
         Double evaluacion,
         String poster,
         Categoria genero,
         String actores,
         String sinopsis
) {
}
