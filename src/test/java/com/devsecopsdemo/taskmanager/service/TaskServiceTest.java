package com.devsecopsdemo.taskmanager.service;

import com.devsecopsdemo.taskmanager.dto.PagedResponse;
import com.devsecopsdemo.taskmanager.dto.TaskRequest;
import com.devsecopsdemo.taskmanager.dto.TaskResponse;
import com.devsecopsdemo.taskmanager.exception.TaskNotFoundException;
import com.devsecopsdemo.taskmanager.model.Task;
import com.devsecopsdemo.taskmanager.model.TaskStatus;
import com.devsecopsdemo.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Spy
    private TaskMapper taskMapper = new TaskMapper();

    @InjectMocks
    private TaskService taskService;

    private Task existingTask;

    @BeforeEach
    void setUp() {
        existingTask = Task.builder()
                .id(1L)
                .title("Write tests")
                .description("Cover the service layer")
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDate.now().plusDays(3))
                .build();
    }

    @Test
    void getAllTasks_returnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Task> page = new PageImpl<>(List.of(existingTask), pageable, 1);
        when(taskRepository.findAll(pageable)).thenReturn(page);

        PagedResponse<TaskResponse> result = taskService.getAllTasks(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Write tests");
    }

    @Test
    void getTaskById_whenExists_returnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        TaskResponse result = taskService.getTaskById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Write tests");
    }

    @Test
    void getTaskById_whenNotFound_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createTask_savesAndReturnsTask() {
        TaskRequest request = new TaskRequest("New task", "Description", TaskStatus.PENDING, LocalDate.now().plusDays(1));
        Task savedTask = Task.builder()
                .id(2L)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .dueDate(request.getDueDate())
                .build();
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse result = taskService.createTask(request);

        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getTitle()).isEqualTo("New task");
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_whenExists_updatesAndReturnsTask() {
        TaskRequest request = new TaskRequest("Updated title", "Updated description", TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(5));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        TaskResponse result = taskService.updateTask(1L, request);

        assertThat(result.getTitle()).isEqualTo("Updated title");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void updateTask_whenNotFound_throwsException() {
        TaskRequest request = new TaskRequest("Updated title", "Updated description", TaskStatus.IN_PROGRESS, LocalDate.now());
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(99L, request))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void deleteTask_whenExists_deletesTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).delete(existingTask);
    }

    @Test
    void deleteTask_whenNotFound_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(99L))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
