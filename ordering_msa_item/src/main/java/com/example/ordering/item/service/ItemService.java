package com.example.ordering.item.service;

import com.example.ordering.item.domain.Item;
import com.example.ordering.item.dto.ItemQupdateDto;
import com.example.ordering.item.dto.ItemReqDto;
import com.example.ordering.item.dto.ItemResDto;
import com.example.ordering.item.dto.ItemSearchDto;
import com.example.ordering.item.repository.ItemRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item create(ItemReqDto itemReqDto) {
        MultipartFile multipartFile = itemReqDto.getItemImage();
        String fileName = multipartFile.getOriginalFilename();
        Item new_item = Item.builder()
                .name(itemReqDto.getName())
                .price(itemReqDto.getPrice())
                .stockQuantity(itemReqDto.getStockQuantity())
                .category(itemReqDto.getCategory())
                .build();
        Item item = itemRepository.save(new_item);
        Path path = Paths.get("/Users/song/Desktop/코딩공부/tmp", item.getId() + "_" + fileName);
        item.setImagePath(path.toString());
        try {
            byte[] bytes = multipartFile.getBytes();
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE); // 없으면 넣고 있으면 덮어쓰기
        } catch (IOException e) {
            throw new IllegalArgumentException("image not available");
        }
        return item;
    }

    public Item delete(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("item not found item"));

        item.deleteItem();
        return item;

    }

    public Resource getImage(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("not found item"));
        String imagePath = item.getImagePath();
        Path path = Paths.get(imagePath);
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("url form is not vaild");
        }
        return resource;
    }

    public Item update(Long id, ItemReqDto itemReqDto) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("not fount item"));


        MultipartFile multipartFile = itemReqDto.getItemImage();
        String fileName = multipartFile.getOriginalFilename();
        Path path = Paths.get("/Users/song/Desktop/코딩공부/tmp", item.getId() + "_" + fileName);
        item.setImagePath(path.toString());
        item.updateItem(itemReqDto.getName(), itemReqDto.getCategory(), itemReqDto.getPrice(), itemReqDto.getStockQuantity(), path.toString());
        try {
            byte[] bytes = multipartFile.getBytes();
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE); // 없으면 넣고 있으면 덮어쓰기
        } catch (IOException e) {
            throw new IllegalArgumentException("image not available");
        }
        return item;


    }

    public List<ItemResDto> findAll(ItemSearchDto itemSearchDto, Pageable pageable) {
//        검색을 위해 specification 객체 사용
//        Specification 객체는 복합한 쿼리를 명세를 이용한 정의하여 쉽게 생성
        Specification<Item> spec = new Specification<Item>() {
            @Override
//            root : 엔티티의 속성을 접근하기 위한 객체,
            public Predicate toPredicate(Root<Item> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (itemSearchDto.getName() != null) {
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + itemSearchDto.getName() + "%"));
                }
                if (itemSearchDto.getCategory() != null) {
                    predicates.add(criteriaBuilder.like(root.get("category"), "%" + itemSearchDto.getCategory() + "%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("delYn"), "N"));
                Predicate[] predicatesArr = new Predicate[predicates.size()];
                for (int i = 0; i < predicates.size(); i++) {
                    predicatesArr[i] = predicates.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicatesArr);

                return predicate;
            }
        };

        Page<Item> items = itemRepository.findAll(spec, pageable);
        List<Item> itemList = items.getContent();
        List<ItemResDto> itemResDtos = new ArrayList<>();
        itemResDtos = itemList.stream()
                .map(i -> ItemResDto.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .price(i.getPrice())
                        .category(i.getCategory())
                        .stockQuantity(i.getStockQuantity())
                        .imagePath(i.getImagePath())
                        .build()).collect(Collectors.toList());

        return itemResDtos;
    }

    public ItemResDto findById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        ItemResDto itemResDto = ItemResDto.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .stockQuantity(item.getStockQuantity())
                .price(item.getPrice())
                .build();
        return itemResDto;
    }

    public void updateQuantity(List<ItemQupdateDto> itemQupdateDtos) {
        for (ItemQupdateDto itemQupdateDto : itemQupdateDtos) {
            Item item = itemRepository.findById(itemQupdateDto.getId()).orElseThrow(EntityNotFoundException::new);
            item.updateStockQuantity(itemQupdateDto.getStockQuantity());
        }
    }


/*
    public List<ItemResDto> findAll(ItemSearchDto itemSearchDto, Pageable pageable) {
        Page<Item> items = itemRepository.findAllByDelYnAndCategoryLikeAndNameLike(
                "N",
                "%" + itemSearchDto.getCategory() + "%",
                "%" + itemSearchDto.getName() + "%",
                pageable
        );

        List<ItemResDto> itemResDtos = items.getContent().stream()
                .map(i -> ItemResDto.builder()
                        .name(i.getName())
                        .price(i.getPrice())
                        .category(i.getCategory())
                        .stockQuantity(i.getStockQuantity())
                        .imagePath(i.getImagePath())
                        .build())
                .collect(Collectors.toList());

        return itemResDtos;
    }*/
}
