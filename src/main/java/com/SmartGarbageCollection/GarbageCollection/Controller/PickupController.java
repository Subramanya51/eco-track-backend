package com.SmartGarbageCollection.GarbageCollection.Controller;

import com.SmartGarbageCollection.GarbageCollection.DTO.PickupRequestDTO;
import com.SmartGarbageCollection.GarbageCollection.DTO.PickupStatusUpdateDTO;
import com.SmartGarbageCollection.GarbageCollection.Entity.Pickup;
import com.SmartGarbageCollection.GarbageCollection.Service.PickupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pickup")
public class PickupController {

    @Autowired
    private PickupService pickupService;

    @PostMapping("/request")
    public ResponseEntity<String> requestPickup(@RequestBody PickupRequestDTO request) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Pickup pickup = pickupService.createPickup(
                userName,
                request.getRequestDate(),
                request.getLatitude(),
                request.getLongitude()
        );

//        return ResponseEntity.ok(pickup.getId());
        return ResponseEntity.ok(String.valueOf(pickup.getId()));
    }
    @PutMapping("/update-status")
    public ResponseEntity<Void> updateStatus(@RequestBody PickupStatusUpdateDTO request)
    {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        pickupService.updateStatus(
                request.getPickupId(),
                request.getStatus(),
                userName, request.getPoints()
        );

        return ResponseEntity.ok().build();
    }
    @GetMapping("/active")
    public ResponseEntity<?> getActivePickup() {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Pickup pickup = pickupService.getActivePickup(userName);

        return ResponseEntity.ok(pickup);
    }
    @GetMapping("/test")
    public String test() {
        return "WORKING";
    }
}