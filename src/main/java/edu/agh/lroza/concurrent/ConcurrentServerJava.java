package edu.agh.lroza.concurrent;

import com.google.common.collect.ImmutableSet;
import edu.agh.lroza.common.*;
import scala.Either;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.Set;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static edu.agh.lroza.common.UtilsJ.*;

public class ConcurrentServerJava implements NoticeBoardServer {
    private final Object o = new Object();
    private ConcurrentMap<UUID, Object> loggedUsers = new ConcurrentHashMap<UUID, Object>();
    private ConcurrentMap<String, Object> titleSet = new ConcurrentHashMap<String, Object>();
    private ConcurrentMap<Id, Notice> notices = new ConcurrentHashMap<Id, Notice>();

    private static class LongId implements Id {
        private static final AtomicLong generator = new AtomicLong();
        private final Long id;

        public static Id get() {
            return new LongId(generator.getAndIncrement());
        }

        private LongId(Long id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LongId) {
                return ((LongId) obj).id.equals(id);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "LongId(" + id + ")";
        }
    }

    public Either<Problem, UUID> login(String username, String password) {
        if (username.equals(password)) {
            UUID token = UUID.randomUUID();
            loggedUsers.put(token, o);
            return UtilsJ.right(token);
        } else {
            return left(newProblem("Wrong password"));
        }
    }

    public Option<Problem> logout(UUID token) {
        if (loggedUsers.remove(token) != null) {
            return Option.empty();
        } else {
            return UtilsJ.some(newProblem("Invalid token"));
        }
    }

    public Either<Problem, Set<Id>> listNoticesIds(UUID token) {
        if (loggedUsers.containsKey(token)) {
            return UtilsJ.right((Set<Id>) JavaConversions.asScalaSet(ImmutableSet.copyOf(notices.keySet())));
        } else {
            return left(newProblem("Invalid token"));
        }
    }

    @Override
    public Either<Problem, Id> addNotice(UUID token, String title, String message) {
        if (loggedUsers.containsKey(token)) {
            if (titleSet.putIfAbsent(title, o) == null) {
                Id id = LongId.get();
                notices.put(id, new NoticeJ(title, message));
                return UtilsJ.right(id);
            } else {
                return left(newProblem("Topic with title '" + title + "' already exists"));
            }
        } else {
            return left(newProblem("Invalid token"));
        }
    }

    @Override
    public Either<Problem, Notice> getNotice(UUID token, Id id) {
        if (loggedUsers.containsKey(token)) {
            Notice notice = notices.get(id);
            if (notice == null) {
                return left(newProblem("There is no such notice '" + id + "'"));
            } else {
                return UtilsJ.right(notice);
            }
        } else {
            return left(newProblem("Invalid token"));
        }
    }

    @Override
    public Either<Problem, Id> updateNotice(UUID token, Id id, String title, String message) {
        if (loggedUsers.containsKey(token)) {
            if (titleSet.putIfAbsent(title, o) != null) {
                return left(newProblem("There is no such notice '" + id + "'"));
            } else {
                Notice previous = notices.replace(id, new NoticeJ(title, message));
                if (previous == null) {
                    titleSet.remove(title);
                    return left(newProblem("There is no such notice '" + id + "'"));
                } else {
                    titleSet.remove(previous.title());
                    return right(id);
                }
            }
        } else {
            return left(newProblem("Invalid token"));
        }
    }

    @Override
    public Option<Problem> deleteNotice(UUID token, Id id) {
        if (loggedUsers.containsKey(token)) {
            Notice previous = notices.remove(id);
            if (previous == null) {
                return some(newProblem("There is no such notice '" + id + "'"));
            } else {
                titleSet.remove(previous.title());
                return none();
            }
        } else {
            return some(newProblem("Invalid token"));
        }
    }
}
