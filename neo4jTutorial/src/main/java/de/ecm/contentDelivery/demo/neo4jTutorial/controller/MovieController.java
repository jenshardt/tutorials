package de.ecm.contentDelivery.demo.neo4jTutorial.controller;

import java.util.Collection;
import java.util.Collections;

import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.ecm.contentDelivery.demo.neo4jTutorial.model.Actor;
import de.ecm.contentDelivery.demo.neo4jTutorial.model.Movie;
import de.ecm.contentDelivery.demo.neo4jTutorial.model.Rating;
import de.ecm.contentDelivery.demo.neo4jTutorial.model.User;
import de.ecm.contentDelivery.demo.neo4jTutorial.repository.ActorRepository;
import de.ecm.contentDelivery.demo.neo4jTutorial.repository.MovieRepository;
import de.ecm.contentDelivery.demo.neo4jTutorial.repository.UserRepository;
import de.ecm.contentDelivery.demo.neo4jTutorial.service.DatabasePopulator;

@Controller
public class MovieController {

    private static final Logger log = LoggerFactory.getLogger(MovieController.class);
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ActorRepository actorRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DatabasePopulator populator;
    @Autowired
    private Session session;



    @RequestMapping(value = "/movies/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public
    @ResponseBody
    Movie getMovie(@PathVariable String id) {
        return IteratorUtil.firstOrNull(findMovieByProperty("id", id));
    }


    @RequestMapping(value = "/movies/{movieId}", method = RequestMethod.GET, headers = "Accept=text/html")
    public String singleMovieView(final Model model, @PathVariable String movieId) {
        User user = addUser(model);
        Movie movie = IteratorUtil.firstOrNull(findMovieByProperty("id", movieId));
        model.addAttribute("id", movieId);
        if (movie != null) {
            model.addAttribute("movie", movie);
            final int stars = movie.getStars();
            model.addAttribute("stars", stars);
            Rating rating = null;
            if (user != null) {
                for (Rating r : user.getRatings()) {
                    if (r.getMovie().equals(movie)) {
                        rating = r;
                        break;
                    }
                }
            }
            if (rating == null) {
                rating = new Rating();
                rating.setMovie(movie);
                rating.setUser(user);
                rating.setStars(stars);
            }
            model.addAttribute("userRating", rating);
        }
        return "/movies/show";
    }

    @RequestMapping(value = "/movies/{movieId}", method = RequestMethod.POST, headers = "Accept=text/html")
    public String updateMovie(Model model, @PathVariable String movieId, @RequestParam(value = "rated", required = false) Integer stars, @RequestParam(value = "comment", required = false) String comment) {
        Movie movie = IteratorUtil.firstOrNull(findMovieByProperty("id", movieId));
        User user = userRepository.getUserFromSession();
        if (user != null && movie != null) {
            int stars1 = stars == null ? -1 : stars;
            String comment1 = comment != null ? comment.trim() : null;
            user.rate(movie, stars1, comment1);
            userRepository.save(user);
        }
        return singleMovieView(model, movieId);
    }

    private User addUser(Model model) {
        User user = userRepository.getUserFromSession();
        model.addAttribute("user", user);
        return user;
    }

    @RequestMapping(value = "/movies", method = RequestMethod.GET, headers = "Accept=text/html")
    public String findMovies(Model model, @RequestParam("q") String query) {
        if (query != null && !query.isEmpty()) {
            //Page<Movie> movies = movieRepository.findByTitleLike(query, new PageRequest(0, 20));
            Iterable<Movie> movies = movieRepository.findByTitleLike("(?i).*" + query + ".*");
            model.addAttribute("movies", IteratorUtil.asCollection(movies));
        } else {
            model.addAttribute("movies", Collections.emptyList());
        }
        model.addAttribute("query", query);
        addUser(model);
        return "/movies/list";
    }

    @RequestMapping(value = "/actors/{id}", method = RequestMethod.GET, headers = "Accept=text/html")
    public String singleActorView(Model model, @PathVariable String id) {
        Actor actor = IteratorUtil.firstOrNull(findActorByProperty("id", id));
        model.addAttribute("actor", actor);
        model.addAttribute("id", id);
        model.addAttribute("roles", IteratorUtil.asCollection(actor.getRoles()));
        addUser(model);
        return "/actors/show";
    }

    @RequestMapping(value = "/admin/populate", method = RequestMethod.GET)
    public String populateDatabase(Model model) {
        Collection<Movie> movies = populator.populateDatabase();
        model.addAttribute("movies", movies);
        addUser(model);
        return "/movies/list";
    }

    @RequestMapping(value = "/admin/clean", method = RequestMethod.GET)
    public String clean(Model model) {
        populator.cleanDb();
        return "movies/list";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        addUser(model);
        return "index";
    }

    public Iterable<Movie> findMovieByProperty(String propertyName, Object propertyValue) {
        return session.loadAll(Movie.class, new Filter(propertyName, propertyValue));
    }

    public Iterable<Actor> findActorByProperty(String propertyName, Object propertyValue) {
        return session.loadAll(Actor.class, new Filter(propertyName, propertyValue));
    }
}