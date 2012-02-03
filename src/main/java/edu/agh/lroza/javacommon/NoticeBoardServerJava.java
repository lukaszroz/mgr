package edu.agh.lroza.javacommon;

import java.util.Set;
import java.util.UUID;

import edu.agh.lroza.common.Id;

public interface NoticeBoardServerJava {
    public UUID login(String username, String password) throws ProblemException;

    public void logout(UUID token) throws ProblemException;

    public Set<Id> listNoticesIds(UUID token) throws ProblemException;

    public Id addNotice(UUID token, String title, String message) throws ProblemException;

    public Notice getNotice(UUID token, Id id) throws ProblemException;

    public Id updateNotice(UUID token, Id id, String title, String message) throws ProblemException;

    public void deleteNotice(UUID token, Id id) throws ProblemException;
}
