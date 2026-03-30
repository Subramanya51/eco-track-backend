//package com.SmartGarbageCollection.GarbageCollection.Repository;
//
//import com.SmartGarbageCollection.GarbageCollection.Entity.Pickup;
//import com.SmartGarbageCollection.GarbageCollection.Entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface PickupRepository extends JpaRepository<Pickup, String>
//{
//    Optional<Pickup> findTopByUserAndStatusIsNullOrderByIdDesc(User user);
//
//}
package com.SmartGarbageCollection.GarbageCollection.Repository;

import com.SmartGarbageCollection.GarbageCollection.Entity.Pickup;
import com.SmartGarbageCollection.GarbageCollection.Entity.PickupStatus;
import com.SmartGarbageCollection.GarbageCollection.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PickupRepository extends JpaRepository<Pickup, String> {

    // Existing
    Optional<Pickup> findTopByUserAndStatusIsNullOrderByIdDesc(User user);

    // 🔥 Admin Stats

    long countByRequestDate(LocalDate date);

    long countByRequestDateAndStatus(LocalDate date, PickupStatus status);

    long countByRequestDateAndStatusIsNull(LocalDate date); // ACTIVE
    Optional<Pickup> findTopByUserAndStatusInOrderByIdDesc(
            User user,
            List<PickupStatus> statuses
    );
}