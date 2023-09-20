package com.game.algo.global.converter;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.entity.Block;
import org.jetbrains.annotations.NotNull;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class BlockArrayConverter implements AttributeConverter<List<Block>, String> {

    private static final String SPLIT_BLOCK_CHAR = ",";

    private static final String SPLIT_STATUS_CHAR = "&";

    @Override
    public String convertToDatabaseColumn(List<Block> blockList) {
//        return attribute.stream().map(String::valueOf).collect(Collectors.joining(SPLIT_CHAR));

        return blockList.stream()
                .map(this::blockToString)
                .collect(Collectors.joining(SPLIT_BLOCK_CHAR));
    }

    @Override
    public List<Block> convertToEntityAttribute(String dbData) {
        if (dbData.equals("")){
            return new ArrayList<>();
        }

        return Arrays.stream(dbData.split(SPLIT_BLOCK_CHAR))
                .map(this::stringToBlock)
                .collect(Collectors.toList());
    }

    @NotNull
    private String blockToString(Block block) {
        return block.getBlockCode(true)
                + SPLIT_STATUS_CHAR
                + (block.isOpen() ? "o" : "c");
    }

    @NotNull
    private Block stringToBlock(String string) {
        String[] split = string.split(SPLIT_STATUS_CHAR);
        int code = Integer.parseInt(split[0]);
        BlockColor blockColor = (code > 0) ? BlockColor.WHITE : BlockColor.BLACK;
        boolean isOpen = split[1].equals("o");
        return Block.create(blockColor, Block.parseBlockCode(code), isOpen);
    }
}
