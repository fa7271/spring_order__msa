package com.example.ordering.item.controller;

import com.example.ordering.common.CommonResponse;
import com.example.ordering.item.domain.Item;
import com.example.ordering.item.dto.ItemReqDto;
import com.example.ordering.item.dto.ItemResDto;
import com.example.ordering.item.dto.ItemSearchDto;
import com.example.ordering.item.service.ItemService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PreAuthorize("hasRole('ADMIN')")

    @PostMapping("/item/create")
    public ResponseEntity<CommonResponse> itemCreate(ItemReqDto itemReqDto) { // json 으로 안 하고 멀티파트로 한다. 이미지 때문에
        Item item = itemService.create(itemReqDto);
        return new ResponseEntity<>(new CommonResponse(HttpStatus.CREATED, "item success created", item.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemResDto>> items(ItemSearchDto itemSearchDto, /*@PageableDefault(size = 5)*/ Pageable pageable) {
        List<ItemResDto> itemResDtos = itemService.findAll(itemSearchDto, pageable);

        return new ResponseEntity<>(itemResDtos, HttpStatus.OK);
    }

//     이미지만 나온다. png도 나옴
    @GetMapping("/item/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        Resource resource = itemService.getImage(id);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/item/{id}/update")
    public ResponseEntity<CommonResponse> itemUpdate(@PathVariable Long id, ItemReqDto itemReqDto) {
        Item item = itemService.update(id,itemReqDto);
        return new ResponseEntity<>(new CommonResponse(HttpStatus.OK, "item succesfully updated", item.getId()), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/item/{id}/delete")
    public ResponseEntity<CommonResponse> itemDelete(@PathVariable Long id) {
        Item item = itemService.delete(id);

        return new ResponseEntity<>(new CommonResponse(HttpStatus.OK, "item success deleted", item.getId()), HttpStatus.OK);
    }
}
