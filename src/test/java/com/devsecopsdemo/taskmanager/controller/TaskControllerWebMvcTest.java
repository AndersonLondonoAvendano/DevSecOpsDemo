package com.devsecopsdemo.taskmanager.controller;

import com.devsecopsdemo.taskmanager.dto.PagedResponse;
import com.devsecopsdemo.taskmanager.dto.TaskRequest;
import com.devsecopsdemo.taskmanager.dto.TaskResponse;
import com.devsecopsdemo.taskmanager.exception.TaskNotFoundException;
import com.devsecopsdemo.taskmanager.model.TaskStatus;
import com.devsecopsdemo.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void getAllTasks_returnsOk() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L)
                .title("Sample task")
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        PagedResponse<TaskResponse> response = PagedResponse.<TaskResponse>builder()
                .content(java.util.List.of(task))
                .pageNumber(0)
                .pageSize(20)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
        when(taskService.getAllTasks(any())).thenReturn(response);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Sample task"));
    }

    @Test
    void getTaskById_whenExists_returnsOk() throws Exception {
        TaskResponse task = TaskResponse.builder()
                .id(1L)
                .title("Sample task")
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        when(taskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getTaskById_whenNotFound_returnsNotFound() throws Exception {
        when(taskService.getTaskById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/tasks/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTask_withValidPayload_returnsCreated() throws Exception {
        TaskRequest request = new TaskRequest("New task", "Description", TaskStatus.PENDING, LocalDate.now().plusDays(1));
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("New task")
                .description("Description")
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .dueDate(request.getDueDate())
                .build();
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New task"));
    }

    @Test
    void createTask_withBlankTitle_returnsBadRequest() throws Exception {
        TaskRequest request = new TaskRequest("", "Description", TaskStatus.PENDING, LocalDate.now());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTask_withValidPayload_returnsOk() throws Exception {
        TaskRequest request = new TaskRequest("Updated", "Updated description", TaskStatus.DONE, LocalDate.now());
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Updated")
                .description("Updated description")
                .status(TaskStatus.DONE)
                .createdAt(LocalDateTime.now())
                .dueDate(request.getDueDate())
                .build();
        when(taskService.updateTask(eq(1L), any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/tasks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void deleteTask_whenExists_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
