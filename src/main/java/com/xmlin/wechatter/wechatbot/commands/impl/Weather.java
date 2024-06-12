package com.xmlin.wechatter.wechatbot.commands.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.xmlin.wechatter.wechatbot.commands.Command;
import com.xmlin.wechatter.wechatbot.commands.ICommand;
import com.xmlin.wechatter.wechatbot.enums.CommandType;
import com.xmlin.wechatter.wechatbot.enums.IsOrNot;
import com.xmlin.wechatter.wechatbot.enums.WeatherType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 天气
 */
@Command(type = CommandType.weather)
@Slf4j
public class Weather implements ICommand
{

    private final String url = "https://api.map.baidu.com/weather/v1/?district_id=%s&data_type=all&ak=%s";
    @Value("${wechatter.baiduAK}")
    private String ak;

    private static final String areaFilePath = "/static/weather_district_id.xlsx";

    private final Map<String, String> cache = new ConcurrentReferenceHashMap<>();

    public String transformAreaName(String areaName) {
        if (cache.isEmpty()) {
            ClassPathResource readFile = new ClassPathResource(areaFilePath);
            ExcelReader reader;
            try {
                reader = ExcelUtil.getReader(readFile.getInputStream());
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<Map<String, Object>> mapList = reader.readAll();
            Map<String, String> collect = mapList.stream().collect(
                    Collectors.toMap(stringObjectMap -> String.valueOf(stringObjectMap.get("district")),
                            stringObjectMap -> String.valueOf(stringObjectMap.get("districtcode")),
                            // fixme 需要解决同名问题
                            (s, s2) -> s));
            cache.putAll(collect);
        }
        String areaCode = cache.get(areaName);
        if (CharSequenceUtil.isBlank(areaCode)) {
            throw new IllegalStateException(StrUtil.format("根据areaName：{}无法获取到对应地区", areaName));
        }
        return areaCode;
    }

    /**
     * 获取天气
     *
     * @param districtId 区县代码
     * @return
     */
    @Override
    public String apply(String districtId) {

        try {
            districtId = validateAndTransformDistrictId(districtId);

            String toUseUrl = String.format(url, districtId, ak);

            String weatherJsonString = HttpUtil.get(toUseUrl, 5000);

            JSONObject weatherJson = JSONUtil.parseObj(weatherJsonString);

            String status = weatherJson.getStr("status");
            // 0 是成功
            if (IsOrNot.否.getValue().equals(status)) {
                return buildWeatherInfo(weatherJson);
            }
            else {
                throw new IllegalStateException(
                        String.format("根据接口：%s未能成功获取到数据，返回值为：%s", toUseUrl, weatherJsonString));
            }
        }
        catch (Exception e) {
            log.error("获取天气信息失败！", e);
            return "获取天气信息失败！";
        }
    }

    private static String buildWeatherInfo(JSONObject weatherJson) {
        // 城市名
        String cityName =
                JSONUtil.getByPath(weatherJson, "result.location.city") + "-" + JSONUtil.getByPath(weatherJson,
                        "result.location.name");
        // 今天最低温度
        String todayTempMin = JSONUtil.getByPath(weatherJson, "result.forecasts[0].low") + "";
        // 今天最高温度
        String todayTempMax = JSONUtil.getByPath(weatherJson, "result.forecasts[0].high") + "";
        String nowWeather = JSONUtil.getByPath(weatherJson, "result.now.text") + "";
        String nowTemp = JSONUtil.getByPath(weatherJson, "result.now.temp") + "";
        String nowFeelsTemp = JSONUtil.getByPath(weatherJson, "result.now.feels_like") + "";
        // 相对湿度
        String nowRh = JSONUtil.getByPath(weatherJson, "result.now.rh") + "%";
        // 今天白天天气
        String dayWeather = JSONUtil.getByPath(weatherJson, "result.forecasts[0].text_day") + "";
        // 今天晚上天气
        String nightWeather = JSONUtil.getByPath(weatherJson, "result.forecasts[0].text_night") + "";
        // 风速
        String nowWind = JSONUtil.getByPath(weatherJson, "result.now.wind_dir") + "" + JSONUtil.getByPath(weatherJson,
                "result.now.wind_class");
        String dayWind =
                JSONUtil.getByPath(weatherJson, "result.forecasts[0].wd_day") + "" + JSONUtil.getByPath(weatherJson,
                        "result.forecasts[0].wc_day");
        String nightWind =
                JSONUtil.getByPath(weatherJson, "result.forecasts[0].wd_night") + "" + JSONUtil.getByPath(weatherJson,
                        "result.forecasts[0].wc_night");

        return String.format("""
                        🏙️ %s
                        📅 %s
                        ---TODAY---
                        🌡️ 温度: %s°C ~ %s°C
                        🌤️ 天气: %s%s -> %s%s
                        🌬️ 风速: %s -> %s
                        ---NOW---
                        🌡️ 温度: %s°C 体感: %s°C
                        🌤️ 天气: %s%s
                        🌬️ 风速: %s
                        💧 相对湿度: %s
                        """, cityName, DateUtil.formatDate(new Date()), todayTempMin, todayTempMax, dayWeather,
                WeatherType.getWeatherEmoji(dayWeather), nightWeather, WeatherType.getWeatherEmoji(nightWeather),
                dayWind, nightWind, nowTemp, nowFeelsTemp, nowWeather, WeatherType.getWeatherEmoji(nowWeather), nowWind,
                nowRh);
    }

    private String validateAndTransformDistrictId(String districtId) {
        // 判断是否符合要求
        try {
            Integer.valueOf(districtId);
        }
        catch (NumberFormatException e) {
            districtId = transformAreaName(districtId);
        }
        catch (Exception e) {
            throw new IllegalStateException("地区输入有误！");
        }
        return districtId;
    }

    @Override
    public boolean checkArgs(String districtId) {
        try {
            transformAreaName(districtId);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
