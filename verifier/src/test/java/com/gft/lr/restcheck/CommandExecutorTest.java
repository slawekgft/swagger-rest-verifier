package com.gft.lr.restcheck;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

/**
 * Created on 09/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandExecutorTest {

    public static final String FILES_PARENT = new File("./").getAbsolutePath() + File.separator + "test/resources/yamls";
    public static final String JSON_FILE_PATH = FILES_PARENT + "/spec0_1.json";
    public static final String YAML_FILE_PATH = FILES_PARENT + "/spec0_1.yaml";

    @Mock
    private RuntimeExecutor runtimeExecutor;

    @Mock
    private Process process;

    private CommandExecutor commandExecutor;

    private List<String> commands;

    @Before
    public void setUp() {
        commands = new ArrayList<>();
        given(process.getOutputStream()).willReturn(new ByteArrayOutputStream());
        given(process.getErrorStream()).willReturn(new ByteArrayInputStream(new byte[]{}));
        given(process.getInputStream()).willReturn(new ByteArrayInputStream(new byte[]{}));
        commandExecutor = new CommandExecutor(runtimeExecutor);
    }

    @Test
    public void shouldConvert() throws Exception {
        // given
        given(runtimeExecutor.exec(anyString())).will(answerOfExecution());

        // when
        File yamlFile = commandExecutor.convert(JSON_FILE_PATH, FILES_PARENT);

        // then
        assertThat(commands).hasSize(1);
        assertThat(commands.get(0)).contains("-i /home/osboxes/IdeaProjects/LR/restwatcher.feat/./test/resources/yamls/spec0_1.json");
        assertThat(commands.get(0)).contains("-o /home/osboxes/IdeaProjects/LR/restwatcher.feat/./test/resources/yamls");
        assertThat(yamlFile.getAbsolutePath()).endsWith(FILES_PARENT + File.separator + CommandExecutor.DEFAULT_SWAGGER_YAML);
    }

    @Test(expected = IOException.class)
    public void shouldConvertWithError() throws Exception {
        // given
        given(runtimeExecutor.exec(anyString())).will(answerOfExecution());
        given(process.exitValue()).willReturn(1);

        // when
        commandExecutor.convert(JSON_FILE_PATH, FILES_PARENT);

        // then
    }

    @Test(expected = IOException.class)
    public void shouldConvertWithUnexpectedInterruption() throws Exception {
        // given
        given(runtimeExecutor.exec(anyString())).will(answerOfExecution());
        given(process.waitFor()).willThrow(InterruptedException.class);

        // when
        commandExecutor.convert(JSON_FILE_PATH, FILES_PARENT);

        // then
    }

    @Test
    public void shouldCompareNoOutputNoErrorCode() throws Exception {
        // given
        given(runtimeExecutor.exec(anyString())).will(answerOfExecution());

        // when
        commandExecutor.compare(JSON_FILE_PATH, YAML_FILE_PATH);

        // then
        verify(runtimeExecutor).exec(anyString());
    }

    private Answer<Process> answerOfExecution() {
        return new Answer<Process>() {
            @Override
            public Process answer(InvocationOnMock invocation) throws Throwable {
                commands.add(invocation.getArgumentAt(0, String.class));

                return process;
            }
        };
    }

}