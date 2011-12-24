package edu.agh.lroza.concurrent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import edu.agh.lroza.common.Id;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.NoticeBoardServerJava;
import edu.agh.lroza.javacommon.ProblemException;

import com.google.common.collect.ImmutableSet;

public class ConcurrentServerJava implements NoticeBoardServerJava {
    private final Object o = new Object();
    private ConcurrentMap<UUID, Object> loggedUsers = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Boolean> titleSet = new ConcurrentHashMap<>();
    private ConcurrentMap<Id, Notice> notices = new ConcurrentHashMap<>();

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

    public UUID login(String username, String password) throws ProblemException {
        if (username.equals(password)) {
            UUID token = UUID.randomUUID();
            loggedUsers.put(token, o);
            return token;
        } else {
            throw new ProblemException("Wrong password");
        }
    }

    public void logout(UUID token) throws ProblemException {
        if (loggedUsers.remove(token) == null) {
            throw new ProblemException("Invalid token");
        }
    }

    public Set<Id> listNoticesIds(UUID token) throws ProblemException {
        if (loggedUsers.containsKey(token)) {
            return ImmutableSet.copyOf(notices.keySet());
        } else {
            throw new ProblemException("Invalid token");
        }
    }

    @Override
    public Id addNotice(UUID token, String title, String message) throws ProblemException {
        if (loggedUsers.containsKey(token)) {
            if (titleSet.putIfAbsent(title, false) == null) {
                Id id = LongId.get();
                notices.put(id, new Notice(title, message));
                return id;
            } else {
                throw new ProblemException("Topic with title '" + title + "' already exists");
            }
        } else {
            throw new ProblemException("Invalid token");
        }
    }

    @Override
    public Notice getNotice(UUID token, Id id) throws ProblemException {
        if (loggedUsers.containsKey(token)) {
            Notice notice = notices.get(id);
            if (notice == null) {
                throw new ProblemException("There is no such notice '" + id + "'");
            } else {
                return notice;
            }
        } else {
            throw new ProblemException("Invalid token");
        }
    }

    @Override
    public Id updateNotice(UUID token, Id id, String title, String message) throws ProblemException {
        if (loggedUsers.containsKey(token)) {
            Notice oldNotice = notices.get(id);
            if (titleSet.putIfAbsent(title, true) != null &&
                    !(oldNotice != null && oldNotice.getTitle().equals(title) && !reserveTitle(title))) {
                throw new ProblemException("There is no such notice '" + id + "'");
            } else {
                Notice previous = notices.replace(id, new Notice(title, message));
                if (previous == null) {
                    titleSet.remove(title);
                    throw new ProblemException("There is no such notice '" + id + "'");
                } else {
                    titleSet.put(title, false);
                    if (!previous.getTitle().equals(title)) {
                        titleSet.remove(previous.getTitle());
                    }
                    return id;
                }
            }
        } else {
            throw new ProblemException("Invalid token");
        }
    }

    private Boolean reserveTitle(String title) {
        Boolean put = titleSet.put(title, true);
        if (put == null) {
            return false;
        } else {
            return put;
        }
    }

    @Override
    public void deleteNotice(UUID token, Id id) throws ProblemException {
        if (loggedUsers.containsKey(token)) {
            Notice previous = notices.remove(id);
            if (previous == null) {
                throw new ProblemException("There is no such notice '" + id + "'");
            } else {
                while (titleSet.containsKey(previous.getTitle()) && !titleSet.remove(previous.getTitle(), false)) {
                }
            }
        } else {
            throw new ProblemException("Invalid token");
        }
    }
}