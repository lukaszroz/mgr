package edu.agh.lroza.locks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import edu.agh.lroza.common.Id;
import edu.agh.lroza.javacommon.Notice;
import edu.agh.lroza.javacommon.NoticeBoardServerJava;
import edu.agh.lroza.javacommon.ProblemException;

import com.google.common.collect.ImmutableSet;

public class CustomLocksServerJava implements NoticeBoardServerJava {
    private java.util.Set<UUID> loggedUsers = new HashSet<>();
    private java.util.Map<Id, Notice> notices = new HashMap<>();

    private ReadWriteLock loggedUsersLock = new ReentrantReadWriteLock();
    private ReadWriteLock noticesLock = new ReentrantReadWriteLock();

    private void validateToken(UUID token) throws ProblemException {
        loggedUsersLock.readLock().lock();
        try {
            if (!loggedUsers.contains(token)) {
                throw new ProblemException("Invalid token");
            }
        } finally {
            loggedUsersLock.readLock().unlock();
        }
    }

    @Override
    public UUID login(String username, String password) throws ProblemException {
        if (username.equals(password)) {
            UUID token = UUID.randomUUID();
            loggedUsersLock.writeLock().lock();
            try {
                loggedUsers.add(token);
            } finally {
                loggedUsersLock.writeLock().unlock();
            }
            return token;
        } else {
            throw new ProblemException("Wrong password");
        }
    }

    @Override
    public void logout(UUID token) throws ProblemException {
        loggedUsersLock.writeLock().lock();
        try {
            if (!loggedUsers.remove(token)) {
                throw new ProblemException("Invalid token");
            }
        } finally {
            loggedUsersLock.writeLock().unlock();
        }
    }

    @Override
    public Set<Id> listNoticesIds(UUID token) throws ProblemException {
        validateToken(token);
        noticesLock.readLock().lock();
        try {
            return ImmutableSet.copyOf(notices.keySet());
        } finally {
            noticesLock.readLock().unlock();
        }
    }

    @Override
    public Id addNotice(UUID token, String title, String message) throws ProblemException {
        validateToken(token);
        Id id = new TitleId(title);
        noticesLock.writeLock().lock();
        try {
            if (!notices.containsKey(id)) {
                notices.put(id, new Notice(title, message));
                return id;
            } else {
                throw new ProblemException("Topic with title '" + title + "' already exists");
            }
        } finally {
            noticesLock.writeLock().unlock();
        }
    }

    @Override
    public Notice getNotice(UUID token, Id id) throws ProblemException {
        validateToken(token);
        noticesLock.readLock().lock();
        try {
            Notice notice = notices.get(id);
            if (notice == null) {
                throw new ProblemException("There is no such notice '" + id + "'");
            } else {
                return notice;
            }
        } finally {
            noticesLock.readLock().unlock();
        }
    }

    @Override
    public Id updateNotice(UUID token, Id id, String title, String message) throws ProblemException {
        validateToken(token);
        noticesLock.writeLock().lock();
        try {
            final Notice notice = notices.get(id);
            if (notice == null) {
                throw new ProblemException("There is no such notice '" + id + "'");
            } else {
                Id newId = new TitleId(title);
                if (!newId.equals(id) && notices.containsKey(newId)) {
                    throw new ProblemException("Topic with title '" + title + "' already exists");
                } else {
                    if (!newId.equals(id)) {
                        notices.remove(id);
                    }
                    notices.put(newId, new Notice(title, message));
                    return newId;
                }
            }
        } finally {
            noticesLock.writeLock().unlock();
        }
    }

    @Override
    public void deleteNotice(UUID token, Id id) throws ProblemException {
        validateToken(token);
        noticesLock.writeLock().lock();
        try {
            if (notices.remove(id) == null) {
                throw new ProblemException("There is no such notice '" + id + "'");
            }
        } finally {
            noticesLock.writeLock().unlock();
        }
    }
}
