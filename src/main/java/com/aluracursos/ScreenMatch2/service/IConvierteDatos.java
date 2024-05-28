package com.aluracursos.ScreenMatch2.service;

public interface IConvierteDatos {

    <T> T obtenerDatos(String json, Class<T> clase);

}
