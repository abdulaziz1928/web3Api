package com.example.demo.web3;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.RawTransaction;
import java.io.IOException;

@RestController
@RequestMapping(path = "api/v1/web3")
public class Web3Controller {
    private final Web3Service web3Service;

    @Autowired
    public Web3Controller(Web3Service web3Service) {
        this.web3Service = web3Service;
    }
    @GetMapping("/address/balance")
    public JSONObject getBalance(@RequestBody JSONObject  address){
        return  web3Service.getBalance(address);
    }

    @GetMapping("/{tokenContract}/balance")
    public JSONObject getERC20Balance(@RequestBody JSONObject  address,@PathVariable String tokenContract) throws Exception {
        return web3Service.getERC20Balance(address,tokenContract);
    }

    @PostMapping("/tx")
    public RawTransaction sendTransaction(@RequestBody JSONObject input) throws IOException {
        return  web3Service.sendTransaction(input);
    }
    @PostMapping("/tx/send")
    public String sendSignedTransaction(@RequestBody JSONObject input) throws Exception {
        return  web3Service.sendSignedTransaction(input);
    }
    @PostMapping("/tx/{tokenContract}/send")
    public String sendSignedERC20Transaction(@RequestBody JSONObject input,@PathVariable String tokenContract) throws Exception {
        return  web3Service.sendSignedERC20Transaction(input,tokenContract);
    }
}
