/*
 *
 * * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Primary
@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {

	@Autowired // (required = true)
	private ModelMapper modelMapper;

	@Autowired
	private RestaurantRepository restaurantRepository;

	private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
		LocalTime openingTime = LocalTime.parse(res.getOpensAt());
		LocalTime closingTime = LocalTime.parse(res.getClosesAt());

		return time.isAfter(openingTime) && time.isBefore(closingTime);
	}

	public List<Restaurant> findAllRestaurantsCloseBy(Double latitude, Double longitude,
			LocalTime currentTime, Double servingRadiusInKms) {

		List<RestaurantEntity> restaurantEntities = restaurantRepository.findAll();

		List<Restaurant> restaurants = new ArrayList<>();

		for (RestaurantEntity restaurantEntity : restaurantEntities) {
			if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude,
					servingRadiusInKms)) {
				restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
			}
		}

		return restaurants;
	}

	private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
			LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {

		if (isOpenNow(currentTime, restaurantEntity)) {
			return GeoUtils.findDistanceInKm(latitude, longitude, restaurantEntity.getLatitude(),
					restaurantEntity.getLongitude()) < servingRadiusInKms;
		}

		return false;
	}

	public List<Restaurant> getAllRestaurants() {
		List<RestaurantEntity> restaurantEntities = restaurantRepository.findAll();

		List<Restaurant> restaurants = new ArrayList<>();

		for (RestaurantEntity restaurantEntity : restaurantEntities) {
			restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
		}

		return restaurants;
	}

}
