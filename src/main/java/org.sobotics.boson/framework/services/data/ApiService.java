package org.sobotics.boson.framework.services.data;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.sobotics.boson.framework.exceptions.StackExchangeApiException;
import org.sobotics.boson.framework.model.stackexchange.*;
import org.sobotics.boson.framework.model.stackexchange.api.*;

public abstract class ApiService {

    public List<Answer> getAnswers(String site) throws IOException {
        return getAnswers(site, 1, 30, null, null, Ordering.DESC, AnswerSorting.ACTIVITY);
    }

    public List<Answer> getAnswers(String site, int page, int pageSize, Instant fromDate) throws IOException {
        return getAnswers(site, page, pageSize, fromDate, Instant.now(), Ordering.DESC, AnswerSorting.ACTIVITY);
    }

    public abstract List<Answer> getAnswers(String site, int page, int pageSize, Instant fromDate, Instant toDate, Ordering order, AnswerSorting sort) throws IOException;

    public List<Answer> getAnswersByCreation(String site, int page, int pageSize, Instant fromDate) throws IOException {
        return getAnswers(site, page, pageSize, fromDate, Instant.now(), Ordering.ASC, AnswerSorting.CREATION);
    }

    public List<Post> getPosts(String site) throws IOException {
        return getPosts(site, 1, 30, null, null, Ordering.DESC, PostSorting.ACTIVITY);
    }

    public List<Post> getPosts(String site, Instant fromDate) throws IOException {
        return getPosts(site, fromDate, Instant.now(), Ordering.DESC, PostSorting.ACTIVITY);
    }

    public List<Post> getPosts(String site, int page, int pageSize, Instant fromDate) throws IOException {
        return getPosts(site, page, pageSize, fromDate, Instant.now(), Ordering.DESC, PostSorting.ACTIVITY);
    }

    public abstract List<Post> getPosts(String site, int page, int pageSize, Instant fromDate, Instant toDate, Ordering order, PostSorting sort) throws IOException;

    public abstract List<Post> getPosts(String site, Instant fromDate, Instant toDate, Ordering order, PostSorting sort) throws IOException;

    public List<Post> getPostsByCreation(String site, Instant fromDate) throws IOException {
        return getPosts(site, fromDate, Instant.now(), Ordering.ASC, PostSorting.CREATION);
    }

    public List<Question> getQuestions(String site, int page, int pageSize, Instant fromDate) throws IOException, StackExchangeApiException {
        return getQuestions(site, page, pageSize, fromDate, null, Ordering.DESC, QuestionSorting.ACTIVITY, new String[0]);
    }

    public abstract List<Question> getQuestions(String site, int page, int pageSize, Instant fromDate, Instant toDate, Ordering order,
                                                QuestionSorting sort, String[] tags) throws IOException, StackExchangeApiException;

    public List<Question> getQuestionsByCreation(String site, int page, int pageSize, Instant fromDate) throws IOException, StackExchangeApiException {
        return getQuestions(site, page, pageSize, fromDate, null, Ordering.ASC, QuestionSorting.CREATION, new String[0]);
    }

    public List<Comment> getComments(String site, int page, int pageSize, Instant fromDate) throws IOException {
        return getComments(site, page, pageSize, fromDate, Instant.now(), Ordering.DESC, CommentSorting.CREATION);
    }

    public abstract List<Comment> getComments(String site, int page, int pageSize, Instant fromDate, Instant toDate, Ordering order, CommentSorting sort) throws IOException;

    public abstract List<Tag> getTags(String site, int page, int pageSize, Instant fromDate, Instant toDate, Ordering order, TagSorting sort, String inName) throws IOException;

    public abstract List<Tag> getTags(String site, Instant fromDate, Instant toDate, Ordering order, TagSorting sort, String inName) throws IOException;

    public List<Tag> getTags(String site, int page, int pageSize, Instant fromDate) throws IOException {
        return getTags(site, page, pageSize, fromDate, Instant.now(), Ordering.DESC, TagSorting.POPULAR, "");
    }

    public List<Tag> getTags(String site, Instant fromDate) throws IOException {
        return getTags(site, fromDate, Instant.now(), Ordering.DESC, TagSorting.POPULAR, "");
    }


}
