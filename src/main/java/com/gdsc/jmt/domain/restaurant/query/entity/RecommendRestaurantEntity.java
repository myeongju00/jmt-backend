package com.gdsc.jmt.domain.restaurant.query.entity;

import com.gdsc.jmt.domain.category.query.entity.CategoryEntity;
import com.gdsc.jmt.domain.restaurant.query.dto.response.FindDetailRestaurantItem;
import com.gdsc.jmt.domain.restaurant.query.dto.response.FindRestaurantItems;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity @Table(name = "tb_recommend_restaurant")
public class RecommendRestaurantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String introduce;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name="category_id", nullable = false)
    private CategoryEntity category;

    @OneToOne
    @JoinColumn(name="restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @OneToMany(
            mappedBy = "recommendRestaurant",
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true
    )
    private List<RestaurantPhotoEntity> pictures = new ArrayList<>();

    @Column(nullable = false)
    private Boolean canDrinkLiquor;

    private String goWellWithLiquor;

    private String recommendMenu;

    private String aggregateId;

    @Builder
    RecommendRestaurantEntity(String introduce,
                              CategoryEntity category,
                              RestaurantEntity restaurant,
                              List<RestaurantPhotoEntity> pictures,
                              Boolean canDrinkLiquor,
                              String goWellWithLiquor,
                              String recommendMenu,
                              String aggregateId) {
        this.introduce = introduce;
        this.category = category;
        this.restaurant = restaurant;
        if(pictures != null)
            initPictures(pictures);
        this.pictures = pictures;
        this.canDrinkLiquor = canDrinkLiquor;
        this.goWellWithLiquor = goWellWithLiquor;
        this.recommendMenu = recommendMenu;
        this.aggregateId = aggregateId;
    }

    private void initPictures(List<RestaurantPhotoEntity> pictures) {
        for(RestaurantPhotoEntity picture : pictures) {
            picture.initRecommendRestaurant(this);
        }
    }
    public FindDetailRestaurantItem toResponse() {
        return new FindDetailRestaurantItem(
                restaurant.getName(),
                restaurant.getPlaceUrl(),
                category.getName(),
                restaurant.getPhone(),
                restaurant.getAddress(),
                restaurant.getRoadAddress(),
                restaurant.getLocation().getX(),
                restaurant.getLocation().getY(),
//                restaurant.getImage(),
                canDrinkLiquor,
                goWellWithLiquor,
                recommendMenu,
                aggregateId
        );
    }

    public FindRestaurantItems convertToFindItems() {
        return new FindRestaurantItems(
                this.id,
                this.restaurant.getName(),
                this.restaurant.getPlaceUrl(),
                this.restaurant.getPhone(),
                this.restaurant.getAddress(),
                this.restaurant.getRoadAddress(),
                this.introduce,
                this.category.getName(),
                this.aggregateId
        );
    }
}
