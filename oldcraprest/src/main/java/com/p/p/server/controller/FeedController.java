package com.p.p.server.controller;

import com.p.p.server.model.bean.Picture;
import com.p.p.server.model.bean.Posting;
import com.p.p.server.model.bean.User;
import com.p.p.server.model.repository.PictureRepository;
import com.p.p.server.model.repository.PostingRepository;
import com.p.p.server.util.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class FeedController {

	@Autowired
	DBUtils dbUtils;

	@Autowired
	PostingRepository postingRepository;

	@Autowired
	PictureRepository pictureRepository;

	@RequestMapping(value = { "/feed" }, method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public List<Posting> feed(@RequestParam(required = false) Long time, @RequestParam(required = false,
	  defaultValue = "128") Integer limit) {

		Date date = time != null ? new Date(time) : Calendar.getInstance().getTime();

		return dbUtils.getPostingsBefore(date, limit);
	}


	@RequestMapping(value = { "/post" }, method = RequestMethod.POST)
	@ResponseBody
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

}
