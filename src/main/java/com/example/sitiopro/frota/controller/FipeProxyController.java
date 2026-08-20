package com.example.sitiopro.frota.controller;

import com.example.sitiopro.frota.dto.MarcaDTO;
import com.example.sitiopro.frota.model.FipeCache;
import com.example.sitiopro.frota.service.FipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fipe")
public class FipeProxyController {

    private final FipeService fipeService;

    public FipeProxyController(FipeService fipeService) {
        this.fipeService = fipeService;
    }

    @GetMapping("/marcas")
    public List<MarcaDTO> getMarcas(@RequestParam Integer tipo) {
        return fipeService.buscarMarcas(tipo);
    }

    @GetMapping("/modelos")
    public List<MarcaDTO> getModelos(@RequestParam Integer tipo, @RequestParam Integer marca) {
        return fipeService.buscarModelos(tipo, marca);
    }

    @GetMapping("/anos")
    public List<Object> getAnos(@RequestParam Integer tipo, @RequestParam Integer marca, @RequestParam Integer modelo) {
        return fipeService.buscarAnos(tipo, marca, modelo);
    }

    @GetMapping("/detalhes/{id}")
    public FipeCache getDetalhes(@PathVariable Integer id) {
        return fipeService.buscarDetalhes(id);
    }
}
