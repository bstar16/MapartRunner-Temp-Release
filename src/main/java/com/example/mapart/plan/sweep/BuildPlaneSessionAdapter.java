package com.example.mapart.plan.sweep;

import com.example.mapart.plan.state.BuildSession;

import java.util.function.IntPredicate;

public final class BuildPlaneSessionAdapter {
    private final BuildSession session;

    public BuildPlaneSessionAdapter(BuildSession session) {
        this.session = session;
    }

    public IntPredicate completionPredicate() {
        int nextIndex = session.getCurrentPlacementIndex();
        return placementIndex -> placementIndex >= 0 && placementIndex < nextIndex;
    }
}
