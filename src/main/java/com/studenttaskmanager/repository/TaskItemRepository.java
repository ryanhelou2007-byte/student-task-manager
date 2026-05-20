package com.studenttaskmanager.repository;

import com.studenttaskmanager.model.TaskItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskItemRepository extends JpaRepository<TaskItem, Long> {

    List<TaskItem> findByUserIdOrderByDueDateAscCreatedAtDesc(Long userId);

    List<TaskItem> findByUserIdAndDueDateBetweenOrderByDueDateAscCreatedAtDesc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<TaskItem> findByIdAndUserId(Long id, Long userId);
}
