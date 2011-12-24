//package edu.agh.lroza.concurrent;
//
//import static edu.agh.lroza.javacommon.UtilsJ.left;
//import static edu.agh.lroza.javacommon.UtilsJ.newProblem;
//import static edu.agh.lroza.javacommon.UtilsJ.none;
//import static edu.agh.lroza.javacommon.UtilsJ.right;
//import static edu.agh.lroza.javacommon.UtilsJ.some;
//
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.atomic.AtomicLong;
//
//import edu.agh.lroza.common.Id;
//import edu.agh.lroza.common.Notice;
//import edu.agh.lroza.common.NoticeBoardServerScala;
//import edu.agh.lroza.javacommon.NoticeJ;
//import edu.agh.lroza.common.Problem;
//import edu.agh.lroza.javacommon.UtilsJ;
//import scala.Either;
//import scala.Option;
//import scala.collection.JavaConversions;
//import scala.collection.Set;
//
//import com.google.common.collect.ImmutableSet;
//
//public class ConcurrentServerJava implements NoticeBoardServerScala {
//    private final Object o = new Object();
//    private ConcurrentMap<UUID, Object> loggedUsers = new ConcurrentHashMap<>();
//    private ConcurrentMap<String, Boolean> titleSet = new ConcurrentHashMap<>();
//    private ConcurrentMap<Id, Notice> notices = new ConcurrentHashMap<>();
//
//    private static class LongId implements Id {
//        private static final AtomicLong generator = new AtomicLong();
//        private final Long id;
//
//        public static Id get() {
//            return new LongId(generator.getAndIncrement());
//        }
//
//        private LongId(Long id) {
//            this.id = id;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (obj instanceof LongId) {
//                return ((LongId) obj).id.equals(id);
//            } else {
//                return false;
//            }
//        }
//
//        @Override
//        public int hashCode() {
//            return id.hashCode();
//        }
//
//        @Override
//        public String toString() {
//            return "LongId(" + id + ")";
//        }
//    }
//
//    public Either<Problem, UUID> login(String username, String password) {
//        if (username.equals(password)) {
//            UUID token = UUID.randomUUID();
//            loggedUsers.put(token, o);
//            return UtilsJ.right(token);
//        } else {
//            return left(newProblem("Wrong password"));
//        }
//    }
//
//    public Option<Problem> logout(UUID token) {
//        if (loggedUsers.remove(token) != null) {
//            return Option.empty();
//        } else {
//            return UtilsJ.some(newProblem("Invalid token"));
//        }
//    }
//
//    public Either<Problem, Set<Id>> listNoticesIds(UUID token) {
//        if (loggedUsers.containsKey(token)) {
//            return UtilsJ.right((Set<Id>) JavaConversions.asScalaSet(ImmutableSet.copyOf(notices.keySet())));
//        } else {
//            return left(newProblem("Invalid token"));
//        }
//    }
//
//    @Override
//    public Either<Problem, Id> addNotice(UUID token, String title, String message) {
//        if (loggedUsers.containsKey(token)) {
//            if (titleSet.putIfAbsent(title, false) == null) {
//                Id id = LongId.get();
//                notices.put(id, new NoticeJ(title, message));
//                return UtilsJ.right(id);
//            } else {
//                return left(newProblem("Topic with title '" + title + "' already exists"));
//            }
//        } else {
//            return left(newProblem("Invalid token"));
//        }
//    }
//
//    @Override
//    public Either<Problem, Notice> getNotice(UUID token, Id id) {
//        if (loggedUsers.containsKey(token)) {
//            Notice notice = notices.get(id);
//            if (notice == null) {
//                return left(newProblem("There is no such notice '" + id + "'"));
//            } else {
//                return UtilsJ.right(notice);
//            }
//        } else {
//            return left(newProblem("Invalid token"));
//        }
//    }
//
//    @Override
//    public Either<Problem, Id> updateNotice(UUID token, Id id, String title, String message) {
//        if (loggedUsers.containsKey(token)) {
//            Notice oldNotice = notices.get(id);
//            if (titleSet.putIfAbsent(title, true) != null &&
//                    !(oldNotice != null && oldNotice.title().equals(title) && !reserveTitle(title))) {
//                return left(newProblem("There is no such notice '" + id + "'"));
//            } else {
//                Notice previous = notices.replace(id, new NoticeJ(title, message));
//                if (previous == null) {
//                    titleSet.remove(title);
//                    return left(newProblem("There is no such notice '" + id + "'"));
//                } else {
//                    titleSet.put(title, false);
//                    if (!previous.title().equals(title)) {
//                        titleSet.remove(previous.title());
//                    }
//                    return right(id);
//                }
//            }
//        } else {
//            return left(newProblem("Invalid token"));
//        }
//    }
//
//    private Boolean reserveTitle(String title) {
//        Boolean put = titleSet.put(title, true);
//        if (put == null) {
//            return false;
//        } else {
//            return put;
//        }
//    }
//
//    @Override
//    public Option<Problem> deleteNotice(UUID token, Id id) {
//        if (loggedUsers.containsKey(token)) {
//            Notice previous = notices.remove(id);
//            if (previous == null) {
//                return some(newProblem("There is no such notice '" + id + "'"));
//            } else {
//                while (titleSet.containsKey(previous.title()) && !titleSet.remove(previous.title(), false)) {
//                }
//                return none();
//            }
//        } else {
//            return some(newProblem("Invalid token"));
//        }
//    }
//}