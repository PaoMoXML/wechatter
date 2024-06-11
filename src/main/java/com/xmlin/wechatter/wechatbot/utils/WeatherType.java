package com.xmlin.wechatter.wechatbot.utils;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WeatherType
{

    //    ☀️☁️⛅⛈️🌤️🌥️🌦️🌧️🌨️🌩️💧❄️☔🌫️🌪️🌬️

    晴("☀️"),
    多云("⛅"),
    阴("☁️"),
    阵雨("🌧️"),
    雷阵雨("⛈️"),
    雷阵雨伴有冰雹("⛈️+"),
    雨夹雪("🌨️"),
    小雨("🌧️"),
    中雨("🌧️"),
    大雨("🌧️"),
    暴雨("🌧️"),
    大暴雨("🌧️"),
    特大暴雨("🌧️"),
    阵雪("🌨️"),
    小雪("🌨️"),
    中雪("🌨️"),
    大雪("🌨️"),
    暴雪("🌨️"),
    雾("🌫️"),
    冻雨("🌧️"),
    沙尘暴("🌬️"),
    小到中雨("🌧️"),
    中到大雨("🌧️"),
    大到暴雨("🌧️"),
    暴雨到大暴雨("🌧️"),
    大暴雨到特大暴雨("🌧️"),
    小到中雪("🌨️"),
    中到大雪("🌨️"),
    大到暴雪("🌨️"),
    浮尘("🌫️"),
    扬沙("🌫️"),
    强沙尘暴("🌫️"),
    浓雾("🌫️"),
    龙卷风("🌪️"),
    弱高吹雪("🌨️"),
    轻雾("🌫️"),
    强浓雾("🌫️"),
    霾("🌫️"),
    中度霾("🌫️"),
    重度霾("🌫️"),
    严重霾("🌫️"),
    大雾("🌫️"),
    特强浓雾("🌫️"),
    雨("☔"),
    雪("❄️");

    String emoji;

    public static String getWeatherEmoji(String emojiName) {
        WeatherType by = EnumUtil.getBy(WeatherType.class, weatherType -> weatherType.name().equals(emojiName));
        if (by == null) {
            return "❓";
        }
        return by.getEmoji();
    }
}
