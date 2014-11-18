package com.p.p.server.controller;

import com.p.p.server.model.bean.PData;
import com.p.p.server.model.bean.Picture;
import com.p.p.server.model.bean.Posting;
import com.p.p.server.model.bean.User;
import com.p.p.server.model.repository.PDataRepository;
import com.p.p.server.model.repository.PictureRepository;
import com.p.p.server.model.repository.PostingRepository;
import com.p.p.server.util.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(value = {"/user"})
public class FeedController {

    @Autowired
    DBUtils dbUtils;

    @Autowired
    PDataRepository pDataRepository;

    @Autowired
    PostingRepository postingRepository;

    @Autowired
    PictureRepository pictureRepository;

    @RequestMapping(value = {"/feed"}, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Posting> feed(@RequestParam(required = false) Long time, @RequestParam(required = false,
            defaultValue = "128") Integer limit) {

        Date date = time != null ? new Date(time) : Calendar.getInstance().getTime();

        return dbUtils.getPostingsBefore(date, limit);
    }


    @RequestMapping(value = {"/post"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void post(String text) {

        Posting posting =
                new Posting((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal(), text);
        postingRepository.save(posting);

        Picture picture = new Picture(posting, text);
        pictureRepository.save(picture);

        posting.getPictures().add(picture);

        postingRepository.save(posting);
    }

    /**
     * Upload multiple file using Spring Controller
     */
    @RequestMapping(value = "/uploadMultipleFile", method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseStatus(HttpStatus.OK)
    public void uploadMultipleFileHandler(@RequestParam("posting") String postingId,
                                          @RequestParam(value = "name", required = false) String[] names,
                                          @RequestParam("file") MultipartFile[] files,
                                          @RequestParam(value = "comment", required = false)String[] comments) {

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            try {
                byte[] bytes = file.getBytes();

                Posting posting = postingRepository.findOne(postingId);

                String comment = "";
                if (comments != null && comments.length > i && comments[i] != null) {
                    comment = comments[i];
                }
                Picture picture = new Picture(posting, comment);

                PData pData = new PData(bytes);
                pDataRepository.save(pData);
                picture.setBlobId(pData.getId());
                pictureRepository.save(picture);
            } catch (Exception e) {
                throw new IllegalArgumentException("Can not save picture!");
            }
        }
    }
}
