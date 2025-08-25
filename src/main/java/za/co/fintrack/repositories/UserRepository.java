package za.co.fintrack.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.fintrack.models.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
