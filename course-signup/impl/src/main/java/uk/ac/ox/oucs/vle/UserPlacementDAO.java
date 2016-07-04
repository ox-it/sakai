package uk.ac.ox.oucs.vle;


public interface UserPlacementDAO {

    CourseUserPlacementDAO findUserPlacement(String userId);

    void save(CourseUserPlacementDAO placementDao);

}
