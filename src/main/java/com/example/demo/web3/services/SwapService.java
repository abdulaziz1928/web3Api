package com.example.demo.web3.services;

import com.example.demo.web3.models.*;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.web3j.contracts.eip20.generated.ERC20;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

@Service
public class SwapService {
    private static final Web3j client = Web3j.build(
            new HttpService("https://eth-rinkeby.alchemyapi.io/v2/F5yu5yvHjAWiRJWLugIWwlJaiW-n4dM7"));
    private static final Web3j clientMain = Web3j.build(
            new HttpService("https://mainnet.infura.io/v3/a9fadbdf4f204dd3aa9b733691763878"));

    private static final String factoryContractAddress = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f";
    private static final String routerContractAddress = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D";

    // private static final Web3j client= Web3j.build(
    // new
    // HttpService("https://rinkeby.infura.io/v3/a9fadbdf4f204dd3aa9b733691763878"));

    // working
    public JSONObject getExchangeRate(JSONObject input) throws Exception {
        String token0 = input.get("token_address0").toString();
        String token1 = input.get("token_address1").toString();
        String privateKeys = input.get("private").toString();
        String amount = input.get("amount_token0").toString();
        Credentials credentials = Credentials.create(privateKeys);

        UniswapV2Router02 contractRouter = UniswapV2Router02.load(routerContractAddress, client, credentials,
                new DefaultGasProvider());
        List r = contractRouter.getAmountsOut(new BigInteger(amount), Arrays.asList(token0, token1)).send();

        JSONObject j = new JSONObject();
        j.put("pair", r);
        j.put("formula_to_use", "(token0Amount * 10 ^ token0Decimals) / (token1Amount * 10 ^ token0Decimals)");
        return j;
    }

    // Working
    public String swapExactEthForToken(JSONObject input) throws Exception {
        String token1= input.get("token_address").toString();
        String privateKeys= input.get("private").toString();
        Credentials credentials= Credentials.create(privateKeys);
        String amnt= input.get("amount_eth").toString();
        BigInteger amount= new BigInteger(amnt);
        DefaultGasProvider def=new DefaultGasProvider();
        StaticGasProvider s=new StaticGasProvider(def.getGasPrice().add(def.getGasPrice().divide(new BigInteger("10"))),def.getGasLimit());
        UniswapV2Router02 contractRouter= UniswapV2Router02.load(routerContractAddress,client,credentials,s);

        List amountsout = contractRouter
                .getAmountsOut(new BigInteger(amnt), Arrays.asList(contractRouter.WETH().send(), token1)).send();
        double samountOutMin = Double.parseDouble(amountsout.get(1).toString()) * 90 / 100;
        long m = ((long) samountOutMin);
        BigInteger amountOutMin = new BigInteger(String.valueOf(m));
        System.out.println(new BigInteger(String.valueOf(m)));
        System.out.println(amountsout);
        System.out.println(amountOutMin);

        return contractRouter.swapExactETHForTokens(amountOutMin, Arrays.asList(contractRouter.WETH().send(), token1),
                credentials.getAddress(), BigInteger.valueOf((System.currentTimeMillis() / 1000) + 60 * 10), amount)
                .sendAsync().get().getTransactionHash().toString();
    }

    // ChainLink Price Feeds Input PrivateKey + Price Feed Contract Address
    public JSONObject getPrice(JSONObject input) throws Exception {
        String contractAddress = input.get("price_feed_address").toString();
        String privateKeys = input.get("private").toString();
        Credentials credentials = Credentials.create(privateKeys);

        AggregatorV3Interface contract = AggregatorV3Interface.load(contractAddress, clientMain, credentials,
                new DefaultGasProvider());

        Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> s = contract.latestRoundData().send();
        double priceAmount = Double.parseDouble(s.component2().toString()) / Math.pow(10, 8);
        JSONObject price = new JSONObject();
        price.put("price", priceAmount);
        price.put("last_update", s.component4());
        return price;
    }

    // Working
    public String swapExactTokensForETH(JSONObject input) throws Exception {
        String token0= input.get("token_address").toString();
        String privateKeys= input.get("private").toString();
        Credentials credentials= Credentials.create(privateKeys);
        String amnt= input.get("amount_token").toString();
        BigInteger amount= new BigInteger(amnt);
        DefaultGasProvider def=new DefaultGasProvider();
        StaticGasProvider s=new StaticGasProvider(def.getGasPrice().add(def.getGasPrice().divide(new BigInteger("10"))),def.getGasLimit());
        UniswapV2Router02 contractRouter= UniswapV2Router02.load(routerContractAddress,client,credentials,s);
        List amountsout= contractRouter.getAmountsOut(new BigInteger(amnt),List.of(token0,contractRouter.WETH().send())).send();
        double samountOutMin= Double.parseDouble(amountsout.get(1).toString()) * 90 / 100;
        long m= ((long) samountOutMin);
        BigInteger amountOutMin=new BigInteger(String.valueOf(m));
        System.out.println(new BigInteger(String.valueOf(m)));
        System.out.println(amountsout);
        System.out.println(amountOutMin);
        ERC20 tokenContract = ERC20.load(token0, client, credentials, new DefaultGasProvider());
        tokenContract.approve(contractRouter.getContractAddress(), amount).send();

        return contractRouter
                .swapExactTokensForETH(amount, amountOutMin, Arrays.asList(token0, contractRouter.WETH().send()),
                        credentials.getAddress(), BigInteger.valueOf((System.currentTimeMillis() / 1000) + 60 * 10))
                .sendAsync().get().getTransactionHash();
    }

    // Working
    public String swapExactTokensForTokens(JSONObject input) throws Exception {
        String token0= input.get("token_address0").toString();
        String token1= input.get("token_address1").toString();
        String privateKeys= input.get("private").toString();
        Credentials credentials= Credentials.create(privateKeys);
        String amnt= input.get("amount_token0").toString();
        BigInteger amount= new BigInteger(amnt);
        DefaultGasProvider def=new DefaultGasProvider();
        StaticGasProvider s=new StaticGasProvider(def.getGasPrice().add(def.getGasPrice().divide(new BigInteger("10"))),def.getGasLimit());

        UniswapV2Router02 contractRouter= UniswapV2Router02.load(routerContractAddress,client,credentials,s);
        List amountsout= contractRouter.getAmountsOut(new BigInteger(amnt),List.of(token0,token1)).send();
        double samountOutMin= Double.parseDouble(amountsout.get(1).toString()) * 90 / 100;
        long m= ((long) samountOutMin);
        BigInteger amountOutMin=new BigInteger(String.valueOf(m));
        System.out.println(new BigInteger(String.valueOf(m)));
        System.out.println(amountsout);
        System.out.println(amountOutMin);
        ERC20 tokenContract = ERC20.load(token0, client, credentials, new DefaultGasProvider());
        tokenContract.approve(contractRouter.getContractAddress(), amount).send();

        return contractRouter
                .swapExactTokensForTokens(amount, amountOutMin, Arrays.asList(token0, token1), credentials.getAddress(),
                        BigInteger.valueOf((System.currentTimeMillis() / 1000) + 60 * 10))
                .sendAsync().get().getTransactionHash();
    }

}
