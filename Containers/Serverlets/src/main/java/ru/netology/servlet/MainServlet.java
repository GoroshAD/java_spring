package ru.netology.servlet;

import ru.netology.constant.HttpMethod;
import ru.netology.controller.PostController;
import ru.netology.repository.PostRepository;
import ru.netology.service.PostService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

public class MainServlet extends HttpServlet {
    private static final String API_PATH = "/api/posts";
    private static final Pattern POSTS_ID_PATTERN = Pattern.compile("/api/posts/\\d+");

    private PostController controller;

    @Override
    public void init() {
        final var repository = new PostRepository();
        final var service = new PostService(repository);
        controller = new PostController(service);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final var path = req.getRequestURI();
            final var method = req.getMethod();

            if (method.equals(HttpMethod.GET) && path.equals(API_PATH)) {
                controller.all(resp);
                return;
            }

            if (method.equals(HttpMethod.GET) && POSTS_ID_PATTERN.matcher(path).matches()) {
                final var id = extractIdFromPath(path);
                controller.getById(id, resp);
                return;
            }

            if (method.equals(HttpMethod.POST) && path.equals(API_PATH)) {
                controller.save(req.getReader(), resp);
                return;
            }

            if (method.equals(HttpMethod.DELETE) && POSTS_ID_PATTERN.matcher(path).matches()) {
                final var id = extractIdFromPath(path);
                controller.removeById(id, resp);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private long extractIdFromPath(String path) {
        String idStr = path.substring(path.lastIndexOf("/") + 1);
        return Long.parseLong(idStr);
    }
}