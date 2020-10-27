package Service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tourGuide.Model.*;
import tourGuide.Proxies.GpsProxy;
import tourGuide.Proxies.RewardProxy;
import tourGuide.Proxies.TripPricerProxy;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.*;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
public class TestRewardsService {

    @TestConfiguration
    static class GpsTestsContextConfiguration {

        @Bean
        public GpsProxyService gpsService() {
            return new GpsProxyServiceImpl();
        }

        @Bean
        public RewardsService gpsRewardsService() {
            return new RewardsService();
        }

        @Bean
        public TripPricerService getTripPricerService() { return new TripPricerServiceImpl(); }

    }

    @Autowired
    private GpsProxyService gpsProxyService;

    @Autowired
    private RewardsService rewardsService;

    @Autowired
    private TripPricerService tripPricerService;

    @MockBean
    private GpsProxy gpsProxy;

    @MockBean
    private RewardProxy rewardProxy;

    @MockBean
    private TripPricerProxy tripPricerProxy;

    @Test
    public void isWithinAttractionProximity() {
        Attraction attraction = new Attraction("Musee", "Paris", "France", 1.0, 2.0);
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    @Test
    public void userGetRewards() {
        AttractionMapper attraction1 = new AttractionMapper("Musee", "Paris", "France", UUID.randomUUID(), 1.0, 2.0);
        List<AttractionMapper> attractions = new ArrayList<>();
        attractions.add(attraction1);
        Mockito.when(gpsProxy.gpsGetAttractions()).thenReturn(attractions);

        UUID userId = UUID.randomUUID();
        LocationMapper locationMapper = new LocationMapper(1.0, 1.0);
        Date dt = new Date();
        VisitedLocationMapper visitedLocationMapper = new VisitedLocationMapper(userId, locationMapper, dt);
        Mockito.when(gpsProxy.gpsGetUserLocation(userId.toString())).thenReturn(visitedLocationMapper);

        RewardPointsMapper rewardPointsMapper = new RewardPointsMapper(765);
        Mockito.when(rewardProxy.getAttractionRewardPoints(anyString(), anyString())).thenReturn(rewardPointsMapper);

        rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsProxyService, rewardsService, tripPricerService);
        User user = new User(userId, "jon", "000", "jon@tourGuide.com");
        Attraction attraction = new Attraction("Musee", "Paris", "France", 1.0, 1.0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        tourGuideService.trackUserLocation(user);

        List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();
        assertTrue(userRewards.size() == 1);
    }

    @Test
    public void nearAllAttractions() {
        List<AttractionMapper> attractionMappers = new ArrayList<>();
        Mockito.when(gpsProxy.gpsGetAttractions()).thenReturn(attractionMappers);

        rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsProxyService, rewardsService,tripPricerService);

        User user = tourGuideService.getAllUsers().get(0);
        RewardPointsMapper rewardPointsMapper = new RewardPointsMapper(555);
        Mockito.when(rewardProxy.getAttractionRewardPoints(anyString(), anyString())).thenReturn(rewardPointsMapper);

        Attraction attraction1 = new Attraction("Musee", "Paris", "France",  1.0, 2.0);
        Attraction attraction2 = new Attraction("Gare", "Paris", "France",  10.0, 2.0);
        List<Attraction> attractions = new ArrayList<>();
        attractions.add(attraction1);
        attractions.add(attraction2);

        rewardsService.calculateRewards(user,attractions);
        List<UserReward> userRewards = tourGuideService.getUserRewards(user);
        tourGuideService.tracker.stopTracking();

        assertEquals(attractions.size(), userRewards.size());
    }

}
