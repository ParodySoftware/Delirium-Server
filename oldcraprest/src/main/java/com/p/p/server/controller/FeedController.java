package com.p.p.server.controller;

import com.p.p.server.model.bean.Posting;
import com.p.p.server.util.DBUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class FeedController {

	@Autowired
	DBUtils dbUtils;

	@RequestMapping(value = { "/feed" }, method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public List<Posting> feed(@RequestParam(required = false) Long time, @RequestParam(required = false,
	  defaultValue = "128") Integer limit) {

		Date date = time != null ? new Date(time) : Calendar.getInstance().getTime();

		return dbUtils.getPostingsBefore(date, limit);
	}
}
