package com.sysc4806;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sysc4806.exceptions.ResourceNotFoundException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by adambatson on 3/2/2017.
 */
@Controller
public class PostController {

    @Autowired
    PostRepository postRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    CommentRepository commentRepo;

    @RequestMapping({"/"})
    public String amaIndex(Model model) {
        model.addAttribute("amas", postRepo.findByPosterIsNotNullAndCompleted(false));
        model.addAttribute("page", "index");
        model.addAttribute("title", "Ongoing AMAs");

        return "ama/index";
    }

    @RequestMapping({"/completed"})
    public String amaCompleted(Model model) {
        model.addAttribute("amas", postRepo.findByPosterIsNotNullAndCompleted(true));
        model.addAttribute("page", "completed");
        model.addAttribute("title", "Completed AMAs");

        return "ama/index";
    }

    @RequestMapping("/following")
    public String following(Model model) {
        List<Post> posts = new ArrayList<>();
        User me = AuthenticationController.CurrentUser();
        for (User u : me.getFollowing()) {
            posts.addAll(postRepo.findByPoster(u));
        }

        model.addAttribute("amas", posts);
        model.addAttribute("page", "following");
        model.addAttribute("title", "AMAs By Those I Follow");
        return "ama/following";
    }

    @GetMapping("/ama/new")
    public String amaForm(Model model) {
        model.addAttribute("ama", new Post());
        model.addAttribute("page", "new");
        model.addAttribute("title", "New Post");
        return "ama/new";
    }

    @RequestMapping(value = "/ama/new", method = RequestMethod.POST)
    public String postNewAma(@RequestParam(value="title") String title,
                                @RequestParam(value="description") String description,
                                @RequestParam(value="tags") String tags) {

        if (title.trim().length() == 0)
            throw new ResourceNotFoundException();

        Post p = new Post(title, AuthenticationController.CurrentUser(), description);
        ArrayList<String> tagsTrimmed = new ArrayList<>();
        //Check if tags is empty or all spaces
        if(!tags.isEmpty() && !tags.matches("\\s*")) {
            for (String s : tags.split(","))
                tagsTrimmed.add(s.trim());
        }
        p.setTags(tagsTrimmed);
        postRepo.save(p);

        return "redirect:/ama/view?id="+new Long(p.getId()).toString();
    }

    @GetMapping("/ama/search")
    public String searchAMA(@RequestParam(value = "searchTerm")String searchTerm, Model model){
        List<Post> amas = postRepo.findByPosterIsNotNull();
        List<Post> relatedAmas = new ArrayList<>();
        searchTerm = searchTerm.toLowerCase();

        for(Post ama : amas){
            if((ama.getTitle().toLowerCase().contains(searchTerm)) ||
                    (ama.getDescription().toLowerCase().contains(searchTerm)))
                relatedAmas.add(ama);
            for(String tag : ama.getTags()){
                if(tag.toLowerCase().contains(searchTerm)){
                    relatedAmas.add(ama);
                    break;
                }
            }
        }

        model.addAttribute("amas", relatedAmas);
        model.addAttribute("page", "index");
        model.addAttribute("title", "Resulting AMAs");
        return "ama/index";
    }

    @RequestMapping("/ama/complete")
    public String complete(@RequestParam(value="id")long id) {
        Post post = postRepo.findOne(id);
        if (post == null)
            throw new ResourceNotFoundException();

        post.setCompleted(true);
        postRepo.save(post);
        return "redirect:/ama/view?id="+new Long(post.getId()).toString();
    }

    @RequestMapping("/ama/up")
    public @ResponseBody VoteParameters upVote(@RequestParam(value="id")long id) {
        Post post = postRepo.findOne(id);
        if (post == null)
            throw new ResourceNotFoundException();

        post.upVote(AuthenticationController.CurrentUser());
        postRepo.save(post);
        return new VoteParameters(post);
    }

    @RequestMapping("/ama/down")
    public @ResponseBody VoteParameters downVote(@RequestParam(value="id")long id) {
        Post post = postRepo.findOne(id);
        if (post == null)
            throw new ResourceNotFoundException();

        post.downVote(AuthenticationController.CurrentUser());
        postRepo.save(post);
        return new VoteParameters(post);
    }

    @GetMapping("/ama/delete")
    public String postDeleteAma(@RequestParam(value="id") Long id, Model model) {
        Post post = postRepo.findOne(id);
        if (post == null) {
            throw new ResourceNotFoundException();
        }

        post.setPoster(null);
        post.setDescription("");
        postRepo.save(post);

        if (commentRepo.findByPostAndParent(post, null).size() == 0) {
            postRepo.delete(post);
        }

        return "redirect:/";
    }

    @PostMapping("/ama/edit")
    public String postEditAma(
            @RequestParam(value="id") Long id,
            @RequestParam(value="content") String content,
            Model model) {
        Post post = postRepo.findOne(id);
        if (post == null) {
            throw new ResourceNotFoundException();
        }

        post.setDescription(content);
        postRepo.save(post);

        return "redirect:/ama/view?id="+new Long(post.getId()).toString();
    }

    @RequestMapping("/ama/view")
    public String viewAMA(@RequestParam(value="id", required = false) Long ama,
                          @RequestParam(value="comment", required = false) Long comment, Model model,
                          @RequestParam(value="sortby", required = false) String sortby) {
        if (ama != null) {
            Post p = postRepo.findOne(ama);
            if (p == null) {
                throw new ResourceNotFoundException();
            } else {
                model.addAttribute("ama", p);
                model.addAttribute("title", p.getTitle());
                model.addAttribute("page", "view");
                if(sortby != null) { //Sort the comments
                    switch(sortby) {
                        case "upvotes":
                            model.addAttribute("comments", commentRepo.findByPostAndParentOrderByVotesDesc(p, null));
                            break;
                        case "newest":
                            model.addAttribute("comments", commentRepo.findByPostAndParentOrderByCreatedDesc(p, null));
                            break;
                        case "oldest":
                            model.addAttribute("comments", commentRepo.findByPostAndParentOrderByCreatedAsc(p, null));
                            break;
                        case "downvotes":
                            model.addAttribute("comments", commentRepo.findByPostAndParentOrderByVotesAsc(p, null));
                            break;
                        default:
                            model.addAttribute("comments", commentRepo.findByPostAndParent(p, null));
                            break;
                    }
                } else
                    model.addAttribute("comments", commentRepo.findByPostAndParent(p, null));
            }
        } else if (comment != null) {
            Comment c = commentRepo.findOne(comment);
            if (c == null) {
                throw new ResourceNotFoundException();
            }

            List<Comment> comments = new ArrayList<>(); comments.add(c);
            model.addAttribute("ama", c.getPost());
            model.addAttribute("title", c.getPost().getTitle());
            model.addAttribute("page", "view");
            model.addAttribute("comments", comments);
        } else {
            throw new ResourceNotFoundException();
        }

        return "ama/view";
    }
}