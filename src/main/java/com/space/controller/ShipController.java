package com.space.controller;

import com.space.model.Ship;
import com.space.service.ShipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/rest/ships")
public class ShipController {

    private final ShipService shipService;

    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }


    @GetMapping()
    public List<Ship> getShips(HttpServletRequest request) {
        return shipService.pageItems(request);
    }

    @PostMapping()
    public ResponseEntity<Ship> addShip(@RequestBody Ship ship) {
        return shipService.addShip(ship, -1);
    }

    @GetMapping("/count")
    public long getCount(HttpServletRequest request) {
        return shipService.getCount(request);
    }

    @GetMapping("{id}")
    public ResponseEntity<Ship> getShip (@PathVariable("id") String id) {
        return shipService.getShip(id);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Ship> deleteShip (@PathVariable("id") String id) {
        return shipService.deleteShip(id);
    }

    @PostMapping("{id}")
    public ResponseEntity<Ship> updateShip (@PathVariable("id") String id, @RequestBody Ship ship) {
        return shipService.updateShip(id, ship);
    }


}
