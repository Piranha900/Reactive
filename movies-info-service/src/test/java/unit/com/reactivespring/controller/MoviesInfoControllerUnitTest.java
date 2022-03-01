package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static com.reactivespring.controller.MoviesInfoControllerIntgTest.MOVIES_INFO_URL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    @Test
    void getAllMoviesInfo(){

        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(moviesInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieinfos));
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMoviesInfoById(){

        var id = "abc";
        var movieinfos = new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(moviesInfoServiceMock.getMovieInfoById(id)).thenReturn(Mono.just(movieinfos));
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void addMoviesInfo(){

        var movieinfos = new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieinfos));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieinfos)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(savedMovieInfo);
                    assertNotNull(savedMovieInfo.getMovieInfoId());
                   assertEquals(savedMovieInfo.getMovieInfoId(), "abc");
                });

    }

    @Test
    void addMoviesInfo_validation(){

        var movieinfos = new MovieInfo("abc", "",
                2005, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

//        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieinfos));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieinfos)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult ->
                {
                    var responsebody = stringEntityExchangeResult.getResponseBody();
                    System.out.println("responseBody : "+ responsebody);
                    var expectedErrorMessage = "movieInfo.name must be present";
                    assertNotNull(responsebody);
                    assertEquals(expectedErrorMessage, "movieInfo.name must be present");
                });
    }

    @Test
    void updateMoviesInfo() {

        var movieInfo = new MovieInfo(null, "Dark Knight Rises1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        var movieInfoId = "abc";

        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.just(new MovieInfo("abc", "Dark Knight Rises1",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));
        //when
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(updatedMovieInfo);
                    assertNotNull(updatedMovieInfo.getMovieInfoId());
                    assertEquals("abc", updatedMovieInfo.getMovieInfoId());
                });
    }
    @Test
    void deleteMoviesInfoById(){

        var movieInfoId = "abc";

        when(moviesInfoServiceMock.deleteMovieInfo(isA(String.class)))
                .thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

}