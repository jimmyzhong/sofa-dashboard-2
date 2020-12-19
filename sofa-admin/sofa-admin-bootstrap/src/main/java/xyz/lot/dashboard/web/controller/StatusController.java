package xyz.lot.dashboard.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class StatusController {

    @ResponseBody
    @RequestMapping(value = "/status",method = RequestMethod.HEAD,produces = MediaType.TEXT_PLAIN_VALUE)
    public String status() {
        return "ok123";
    }

}