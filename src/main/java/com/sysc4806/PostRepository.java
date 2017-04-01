package com.sysc4806;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by richardcarson3 on 3/2/2017.
 */
@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
    public List<Post> findByPoster(User poster);
    public List<Post> findByPosterIsNotNull();
}