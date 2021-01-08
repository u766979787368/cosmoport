package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;

import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.*;

@Service
public class ShipService {

    private final ShipRepository repository;

    public ShipService(ShipRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity<Ship> getShip(String id) {
        //gitTest
        if (notValidId(id)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        try {
            Optional<Ship> optional = repository.findById(Long.parseLong(id));
            return new ResponseEntity<>(optional.get(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Ship> deleteShip(String id) {
        if (notValidId(id)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        try {
            repository.deleteById(Long.parseLong(id));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Ship> addShip(Ship ship, long id) {

        if (ship.getProdDate() == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        int year = ship.getProdDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear();
        if (ship.getName().equals("") ||
                ship.getName().length() > 50 ||
                ship.getPlanet().equals("") ||
                ship.getPlanet().length() > 50 ||
                year < 2800 ||
                year > 3019 ||
                ship.getCrewSize() == null ||
                ship.getCrewSize() < 1 ||
                ship.getCrewSize() > 9999 ||
                ship.getSpeed() == null ||
                ship.getSpeed() < 0.01 ||
                ship.getSpeed() > 0.99) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            if (ship.getUsed() == null) ship.setUsed(false);
            if (id != -1) ship.setId(id);
            double k = ship.getUsed() ? 0.5 : 1.0;
            Double rating = new BigDecimal(80 * ship.getSpeed() * k / (3019 - year + 1)).setScale(2, RoundingMode.HALF_UP).doubleValue();
            ship.setRating(rating);
            return new ResponseEntity<>(repository.save(ship), HttpStatus.OK);
        }

    }

    public ResponseEntity<Ship> updateShip(String id, Ship newShip) {
        ResponseEntity<Ship> entity = getShip(id);
        if (entity.getStatusCode() != HttpStatus.OK) return entity;
        Ship lastShip = entity.getBody();
        if (newShip.getName() != null) lastShip.setName(newShip.getName());
        if (newShip.getPlanet() != null) lastShip.setPlanet(newShip.getPlanet());
        if (newShip.getShipType() != null) lastShip.setShipType(newShip.getShipType());
        if (newShip.getProdDate() != null) lastShip.setProdDate(newShip.getProdDate());
        if (newShip.getUsed() != null) lastShip.setUsed(newShip.getUsed());
        if (newShip.getSpeed() != null) lastShip.setSpeed(newShip.getSpeed());
        if (newShip.getCrewSize() != null) lastShip.setCrewSize(newShip.getCrewSize());
        return addShip(lastShip, Long.parseLong(id));
    }

    public boolean notValidId(String id) {
        long trueId;
        try {
            trueId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return true;
        }
        return trueId <= 0;
    }

    public List<Ship> pageItems(HttpServletRequest request) {


        String pageNumberStr = request.getParameter("pageNumber");
        String pageSizeStr = request.getParameter("pageSize");
        String order = request.getParameter("order");


        if (order == null) order = "ID";
        int pageNumber = 0;
        int pageSize = 3;
        if (pageNumberStr != null) {
            pageNumber = Integer.parseInt(pageNumberStr);
        }
        if (pageSizeStr != null) {
            pageSize = Integer.parseInt(pageSizeStr);
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(ShipOrder.valueOf(order).getFieldName()));
        Page<Ship> allProducts = repository.findAll(new ShipSpecification(request), pageable);
        return allProducts.getContent();


    }

    public int getCount(HttpServletRequest request) {
        return repository.findAll(new ShipSpecification(request)).size();
    }

    public class ShipSpecification implements Specification<Ship> {

        HttpServletRequest request;

        public ShipSpecification(HttpServletRequest request) {
            this.request = request;
        }


        @Override
        public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            String name = request.getParameter("name");
            String planet = request.getParameter("planet");
            String shipType = request.getParameter("shipType");
            String after = request.getParameter("after");
            String before = request.getParameter("before");
            String isUsed = request.getParameter("isUsed");
            String minSpeed = request.getParameter("minSpeed");
            String maxSpeed = request.getParameter("maxSpeed");
            String minCrewSize = request.getParameter("minCrewSize");
            String maxCrewSize = request.getParameter("maxCrewSize");
            String minRating = request.getParameter("minRating");
            String maxRating = request.getParameter("maxRating");

            List<Predicate> predicates = new ArrayList<>();
            if (name != null) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
            }
            if (planet != null) {
                predicates.add(criteriaBuilder.like(root.get("planet"), "%" + planet + "%"));
            }
            if (shipType != null) {
                predicates.add(criteriaBuilder.equal(root.get("shipType"), ShipType.valueOf(shipType)));
            }
            if (after != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), new Date(Long.parseLong(after))));
            }
            if (before != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), new Date(Long.parseLong(before))));
            }
            if (isUsed != null) {
                predicates.add(criteriaBuilder.equal(root.get("isUsed"), Boolean.parseBoolean(isUsed)));
            }
            if (minSpeed != null) {
                predicates.add(criteriaBuilder.ge(root.get("speed"), Double.parseDouble(minSpeed)));
            }
            if (maxSpeed != null) {
                predicates.add(criteriaBuilder.le(root.get("speed"), Double.parseDouble(maxSpeed)));
            }
            if (minCrewSize != null) {
                predicates.add(criteriaBuilder.ge(root.get("crewSize"), Integer.parseInt(minCrewSize)));
            }
            if (maxCrewSize != null) {
                predicates.add(criteriaBuilder.le(root.get("crewSize"), Integer.parseInt(maxCrewSize)));
            }
            if (minRating != null) {
                predicates.add(criteriaBuilder.ge(root.get("rating"), Double.parseDouble(minRating)));
            }
            if (maxRating != null) {
                predicates.add(criteriaBuilder.le(root.get("rating"), Double.parseDouble(maxRating)));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }
    }
}
