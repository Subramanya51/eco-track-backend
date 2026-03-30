package com.SmartGarbageCollection.GarbageCollection.Service;

import com.SmartGarbageCollection.GarbageCollection.Entity.Pickup;
import com.SmartGarbageCollection.GarbageCollection.Entity.PickupStatus;
import com.SmartGarbageCollection.GarbageCollection.Entity.User;
import com.SmartGarbageCollection.GarbageCollection.Repository.PickupRepository;
import com.SmartGarbageCollection.GarbageCollection.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.SmartGarbageCollection.GarbageCollection.DTO.FirebasePickupDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PickupService {

    @Autowired
    private PickupRepository pickupRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ CREATE PICKUP
//    public Pickup createPickup(String userName, LocalDate date, Double lat, Double lon)
//    {
//
//        User user = userRepository.findByUserName(userName)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//
//        // 🔒 Prevent multiple active pickups
//        Optional<Pickup> activePickup =
//                pickupRepository.findTopByUserAndStatusIsNullOrderByIdDesc(user);
//
//        if (activePickup.isPresent()) {
//            throw new RuntimeException("Previous pickup not completed");
//        }
//
//        Pickup pickup = new Pickup();
//        pickup.setUser(user);
//        pickup.setRequestDate(date);
//        pickup.setLatitude(lat);
//        pickup.setLongitude(lon);
//        pickup.setStatus(null); // initially empty
//
//        Pickup savedPickup= pickupRepository.save(pickup);
//        pushToFirebase(savedPickup);
//        return savedPickup;
//    }
    public Pickup createPickup(String userName, LocalDate date, Double lat, Double lon) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔒 Check active pickups (NOT COMPLETED)
        Optional<Pickup> activePickup =
                pickupRepository.findTopByUserAndStatusInOrderByIdDesc(
                        user,
                        List.of(PickupStatus.REQUESTED, PickupStatus.IN_PROGRESS)
                );

        if (activePickup.isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Previous pickup not completed"
            );
        }

        // ✅ Create new pickup
        Pickup pickup = new Pickup();
        pickup.setUser(user);
        pickup.setRequestDate(date);
        pickup.setLatitude(lat);
        pickup.setLongitude(lon);

        // 🔥 IMPORTANT FIX
        pickup.setStatus(PickupStatus.REQUESTED);

        Pickup savedPickup = pickupRepository.save(pickup);

        pushToFirebase(savedPickup);

        return savedPickup;
    }

    // ✅ UPDATE STATUS (COLLECTED / NOT_COLLECTED)
    public void updateStatus(String pickupId, String status, String userName,Integer points)
    {

        Pickup pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new RuntimeException("Pickup not found"));

        // 🔐 Ownership check
        if (!pickup.getUser().getUserName().equals(userName)) {
            throw new RuntimeException("Unauthorized");
        }

        // 🔒 Prevent multiple updates
        if (pickup.getStatus() != null) {
            throw new RuntimeException("Status already updated");
        }

        pickup.setStatus(PickupStatus.valueOf(status));
        pickupRepository.save(pickup);
        pickup.setStatus(PickupStatus.valueOf(status));
        pickupRepository.save(pickup);

        if (PickupStatus.valueOf(status) == PickupStatus.COLLECTED) {

            User user = pickup.getUser();

            // 🔹 ECO POINTS
            int currentPoints = user.getEcoPoints() != null ? user.getEcoPoints() : 0;
            int safePoints = (points != null && points > 0) ? points : 0;
            user.setEcoPoints(currentPoints + safePoints);

            // 🔹 TOTAL PICKUPS
            int currentPickups = user.getTotalPickups() != null ? user.getTotalPickups() : 0;
            user.setTotalPickups(currentPickups + 1);
            user.setLastPickupDate(pickup.getRequestDate());

            // ✅ SAVE ONCE
            userRepository.save(user);
        }

        updateFirebaseStatus(pickupId, status);
        updateFirebaseStatus(pickupId,status);
    }

    // ✅ GET ACTIVE PICKUP (status = null)
    public Pickup getActivePickup(String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return pickupRepository
                .findTopByUserAndStatusIsNullOrderByIdDesc(user)
                .orElse(null);
    }
    private void updateFirebaseStatus(String pickupId, String status) {

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("pickupRequests");

        ref.child(pickupId)
                .child("status")
                .setValueAsync(status);
    }
//    private void pushToFirebase(Pickup pickup) {
//
//        DatabaseReference ref = FirebaseDatabase
//                .getInstance()
//                .getReference("pickupRequests");
//
//        ref.child(pickup.getId()).setValueAsync(
//                new FirebasePickupDTO(
//                        pickup.getId(),
//                        pickup.getUser().getId(),
//                        pickup.getRequestDate().toString(),
//                        pickup.getLatitude(),
//                        pickup.getLongitude(),
//                        "REQUESTED"
//                )
//        );
//    }
    private void pushToFirebase(Pickup pickup) {

        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("pickupRequests");

        ref.child(pickup.getId())
                .setValueAsync(
                        new FirebasePickupDTO(
                                pickup.getId(),
                                pickup.getUser().getId(),
                                pickup.getRequestDate().toString(),
                                pickup.getLatitude(),
                                pickup.getLongitude(),
                                "REQUESTED"
                        )
                );

        System.out.println("🔥 PUSH SENT TO FIREBASE");
    }
}