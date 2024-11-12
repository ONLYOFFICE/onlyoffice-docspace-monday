package com.onlyoffice.tenant.service.command;

import com.onlyoffice.common.tenant.transfer.request.command.RegisterRoom;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

public interface BoardCommandService {
  void register(@Valid RegisterRoom command, @NotNull Set<String> docSpaceUsers);
}
