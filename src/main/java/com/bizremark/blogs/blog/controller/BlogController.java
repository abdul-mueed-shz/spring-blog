package com.bizremark.blogs.blog.controller;

import com.bizremark.blogs.blog.constants.Endpoints;
import com.bizremark.blogs.blog.dto.BlogDto;
import com.bizremark.blogs.blog.dto.BlogFilterDto;
import com.bizremark.blogs.blog.info.BlogInfo;
import com.bizremark.blogs.blog.info.BlogResponse;
import com.bizremark.blogs.blog.mapper.BlogDtoMapper;
import com.bizremark.blogs.blog.service.BlogService;
import com.bizremark.blogs.common.config.UserDetailService;
import com.bizremark.blogs.common.dto.PageRequestDto;
import com.bizremark.blogs.user.info.LoggedInUserInfo;
import com.bizremark.blogs.user.mapper.LoggedInUserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/blogs")
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;
    private final BlogDtoMapper blogDtoMapper;
    private final UserDetailService userDetailService;
    private final LoggedInUserMapper loggedInUserMapper;

    @GetMapping
    public ResponseEntity<Page<BlogResponse>> getBlogs(BlogFilterDto filterDto, PageRequestDto pageRequestDto) {
        return ResponseEntity.ok(blogService.getBlogs(filterDto, pageRequestDto));
    }

    @GetMapping(path = Endpoints.USER_BLOGS)
    public ResponseEntity<Page<BlogResponse>> getUserBlogs(@PathVariable("username") String username,
                                                           PageRequestDto pageRequestDto) {
        return ResponseEntity.ok(blogService.getUserBlogs(username, pageRequestDto));
    }

    @GetMapping(path = "{blogId}")
    public ResponseEntity<BlogResponse> getBlog(@PathVariable("blogId") Long blogId) {
        return ResponseEntity.ok(blogService.getBlog(blogId));
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createBlog(@Valid @ModelAttribute BlogDto blogDto) {
        LoggedInUserInfo loggedInUserInfo = userDetailService.getLoggedInUserInfo();
        BlogInfo blogInfo = blogDtoMapper.blogDtoToBlogInfo(blogDto);
        blogInfo.setUser(loggedInUserMapper.loggedInUserInfoToUser(loggedInUserInfo));
        blogService.createBlog(blogInfo, blogDto.getThumbnail());
    }

    @DeleteMapping(path = "{blogId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteBlog(@PathVariable("blogId") Long blogId) {
        LoggedInUserInfo loggedInUserInfo = userDetailService.getLoggedInUserInfo();
        blogService.deleteBlog(blogId, loggedInUserInfo);
    }

    @PutMapping(path = "{blogId}", consumes = "multipart/form-data")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateBlog(@PathVariable("blogId") Long blogId, @Valid @ModelAttribute BlogDto blogDto) {
        LoggedInUserInfo loggedInUserInfo = userDetailService.getLoggedInUserInfo();
        BlogInfo blogInfo = blogDtoMapper.blogDtoToBlogInfo(blogDto);
        blogInfo.setUser(loggedInUserMapper.loggedInUserInfoToUser(loggedInUserInfo));
        blogService.updateBlog(blogId, blogInfo, blogDto.getThumbnail());
    }
}
