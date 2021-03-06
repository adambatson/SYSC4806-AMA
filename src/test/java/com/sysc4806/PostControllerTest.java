package com.sysc4806;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by adambatson on 3/2/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Test
    @WithMockUser
    public void shouldReturnAMAIndex() throws Exception {
        this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void shouldReturnNewAMAFrom() throws Exception {
        mockMvc.perform(get("/ama/new")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void shouldCreateANewPost() throws Exception {
        mockMvc.perform(post("/ama/new")
                    .param("title", "new AMA")
                    .param("description", "Ask me Anything!")
                    .param("tags", "AMA, X, Y, adam"))
                .andReturn();

        List<Post> posts = postRepository.findByPosterIsNotNull();
        assert(posts.size() != 0);
    }

    @Test
    @WithMockUser
    public void shouldRenderAnAMAView() throws Exception {
        shouldCreateANewPost();
        mockMvc.perform(get("/ama/view?id=1")).andExpect(status().isOk());
}

    @Test
    @WithMockUser
    public void should404OnMissingAMA() throws Exception {
        mockMvc.perform((get("/ama/view?id=66")))
                .andExpect(status().is4xxClientError());
    }
}
