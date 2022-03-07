package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;
    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                .flatMap(reviewReactiveRepository::save)
                .flatMap(ServerResponse
                        .status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {

        var movieInfoId = request.queryParam("movieInfoId");
        Flux<Review> reviewFlux;
        if (movieInfoId.isPresent()){
            reviewFlux = reviewReactiveRepository
                    .findReviewByMovieInfoId(Long.valueOf(movieInfoId.get()));
        } else{
            reviewFlux = reviewReactiveRepository.findAll();
        }
        return buildReviewResponse(reviewFlux);
    }

    private Mono<ServerResponse> buildReviewResponse(Flux<Review> reviewFlux) {
        return ServerResponse.ok().body(reviewFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);

        return existingReview.flatMap(review ->
           request.bodyToMono(Review.class)
                   .map(reqReview -> {
                       review.setComment(reqReview.getComment());
                       review.setRating(reqReview.getRating());
                       return review;
                   })
                   .flatMap(reviewReactiveRepository::save)
                   .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview)));
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);
        return existingReview
                .flatMap(review ->
                        reviewReactiveRepository.deleteById(reviewId)
                                .then(ServerResponse.noContent().build()));
    }
}
