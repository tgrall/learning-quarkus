package org.windr.demo;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/v1/boards")
@Produces(MediaType.APPLICATION_JSON)
public class BoardResource {

    @GET
    public List<BoardProjection> allBoards() {
        PanacheQuery<Board> boards = Board.findAll();
        return boards.project(BoardProjection.class).list();
    }

    @GET
    @Path("{id}")
    public BoardProjection getBoard(Long id) {
        PanacheQuery<BoardProjection> boardQuery =
                Board.find("id", id).project(BoardProjection.class);
        return boardQuery.firstResult();
    }

}
