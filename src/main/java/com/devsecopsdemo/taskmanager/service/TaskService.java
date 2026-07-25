package com.devsecopsdemo.taskmanager.service;

import com.devsecopsdemo.taskmanager.dto.PagedResponse;
import com.devsecopsdemo.taskmanager.dto.TaskRequest;
import com.devsecopsdemo.taskmanager.dto.TaskResponse;
import com.devsecopsdemo.taskmanager.exception.TaskNotFoundException;
import com.devsecopsdemo.taskmanager.model.Task;
import com.devsecopsdemo.taskmanager.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final EntityManager entityManager;

    public PagedResponse<TaskResponse> getAllTasks(Pageable pageable) {
        Page<TaskResponse> page = taskRepository.findAll(pageable)
                .map(taskMapper::toResponse);
        return PagedResponse.from(page);
    }

    public TaskResponse getTaskById(Long id) {
        Task task = findTaskOrThrow(id);
        return taskMapper.toResponse(task);
    }

    public TaskResponse createTask(TaskRequest request) {
        Task task = taskMapper.toEntity(request);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTaskOrThrow(id);
        taskMapper.updateEntity(task, request);
        Task updated = taskRepository.save(task);
        return taskMapper.toResponse(updated);
    }

    // INTENTIONAL VULNERABILITY (SQL injection via string-concatenated native query) -
    // added only to validate that the SAST pipeline (Semgrep/CodeQL/SpotBugs+FindSecBugs)
    // actually detects real findings. Remove once verified.
    @SuppressWarnings("unchecked")
    public List<TaskResponse> searchTasksByTitle(String title) {
        String sql = "SELECT * FROM tasks WHERE title LIKE '%" + title + "%'";
        List<Task> tasks = entityManager.createNativeQuery(sql, Task.class).getResultList();
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    public void deleteTask(Long id) {
        Task task = findTaskOrThrow(id);
        taskRepository.delete(task);
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
